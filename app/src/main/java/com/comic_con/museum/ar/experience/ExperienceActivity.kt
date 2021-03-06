package com.comic_con.museum.ar.experience

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.MenuItem
import com.comic_con.museum.ar.CCMApplication
import com.comic_con.museum.ar.MainActivity
import com.comic_con.museum.ar.R
import com.comic_con.museum.ar.experience.nav.BottomNavListener
import com.comic_con.museum.ar.experience.nav.BottomNavOnPageChangeListener
import com.comic_con.museum.ar.experience.progress.ProgressViewModel
import com.comic_con.museum.ar.overview.ExhibitModel
import com.comic_con.museum.ar.overview.OverviewViewModel
import com.comic_con.museum.ar.views.ExhibitCard
import com.google.gson.Gson
import com.unity3d.player.UnityPlayer
import java.lang.IllegalStateException
import javax.inject.Inject


class ExperienceActivity: AppCompatActivity() {

    var toolbar: ActionBar? = null

    @Inject
    lateinit var experienceViewModel: ExperienceViewModel

    @Inject
    lateinit var progressViewModel: ProgressViewModel

    private var experienceModel: ExhibitModel? = null

    // The unity player for the AR component
    val unityPlayer: UnityPlayer by lazy {
        UnityPlayer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CCMApplication.getApplication().injectorComponent.inject(this)

        // Get the experience model associated with the selected experience
        val experienceRes = intent?.extras?.getInt(MainActivity.EXPERIENCE_RESOURCE_KEY) ?: throw IllegalStateException("Experience was started with null experienceId")
        val experienceModel = Gson().fromJson(resources.openRawResource(experienceRes).bufferedReader(), ExhibitModel::class.java)
        experienceViewModel.setExperience(experienceModel)
        this.experienceModel = experienceModel

        // Set up the toolbar
        toolbar = supportActionBar
        toolbar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.show()

        // Set up the experience Progress ViewModel with the initial model if needed
        progressViewModel.getExperienceProgressLiveData(experienceModel.exhibitId, experienceModel.progress)

        setContentView(R.layout.activity_experiences)

        val frag = ExperienceFragment()
        frag.experienceViewModel = experienceViewModel
        switchToFragment(frag, "Experience")
    }

    override fun onResume() {
        super.onResume()

        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNav)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        bottomNavBar?.setOnNavigationItemSelectedListener(BottomNavListener(viewPager))
        viewPager.addOnPageChangeListener(BottomNavOnPageChangeListener(bottomNavBar))
    }

    /**
     * Called (from Unity) when a new item is collected
     * @param contentId: The contentId of the content item collected
     * @return Int correlating to how the collection event was handled
     *  *Success Codes*
     *  0:   New content item was collected, and associated progress items were updated
     *  1:   New content item was collected, but not associated with a progress item
     *  2:   Content item was already collected
     *  *Error Codes*
     *  500: Experience was null in the activity, so progress could not be added to an associated experience
     *  501: Experience does not contain the given Content ID
     */
    @Suppress("unused")
    fun newCollectionEvent(contentId: String): Int {
        this.experienceModel?.exhibitId?.let { experienceId ->
            // If the contentId is not associated with the experience
            if( experienceModel?.content?.contentItems?.asSequence()?.map{ it.contentId }?.contains(contentId) != true ) {
                return 501
            }
            // If the content item was already collected
            if( experienceModel?.progress?.achievedContentItems?.contains(contentId) == true ) {
                return 2
            }
            // If the contentId is not found as an item associated with a progress
            if( experienceModel?.progress?.progressItems?.flatMap{ it.contentItems }?.contains(contentId) != true ) {
                return 1
            }
            // If uncollected new item
            progressViewModel.updateProgress(experienceId, contentId)
            return 0
        }
        return 500
    }

    private fun switchToFragment(fragment: Fragment, tag: String?) {
        val transaction = supportFragmentManager?.beginTransaction() ?: return
        transaction.replace(R.id.content_frame, fragment)
        transaction.addToBackStack(tag)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when( item.itemId ) {
            android.R.id.home -> this.finish()
        }
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish()
            true
        } else super.onKeyDown(keyCode, event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        unityPlayer.windowFocusChanged(hasFocus)
    }

    companion object {

        fun createIntent(context: Context): Intent {
            return Intent(context, ExperienceActivity::class.java)
        }
    }
}