package io.github.wykopmobilny.ui.modules.addlink

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import io.github.wykopmobilny.R
import io.github.wykopmobilny.api.responses.NewLinkResponse
import io.github.wykopmobilny.base.BaseActivity
import io.github.wykopmobilny.databinding.ActivityAddlinkBinding
import io.github.wykopmobilny.ui.modules.addlink.fragments.confirmdetails.AddLinkDetailsFragment
import io.github.wykopmobilny.ui.modules.addlink.fragments.duplicateslist.AddLinkDuplicatesListFragment
import io.github.wykopmobilny.ui.modules.addlink.fragments.urlinput.AddlinkUrlInputFragment
import io.github.wykopmobilny.utils.viewBinding

class AddlinkActivity : BaseActivity() {

    companion object {
        fun createIntent(context: Context) = Intent(context, AddlinkActivity::class.java)
    }

    var draft: NewLinkResponse? = null

    private val binding by viewBinding(ActivityAddlinkBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar.toolbar)
        supportActionBar?.title = getString(R.string.add_new_link)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        var url = ""
        if (intent.action == Intent.ACTION_SEND && intent.type != null) {
            if (intent.type == "text/plain") {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                text?.let {
                    url = text
                }
            }
        }
        if (savedInstanceState == null) {
            openFragment(AddlinkUrlInputFragment.newInstance(url), "url_input")
        }
    }

    private fun openFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentView, fragment, tag)
            // Add this transaction to the back stack
            .addToBackStack(null)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    fun openDuplicatesActivity(response: NewLinkResponse) {
        draft = response
        openFragment(AddLinkDuplicatesListFragment.newInstance(), "duplicates_list")
    }

    fun openDetailsScreen() = openFragment(AddLinkDetailsFragment.newInstance(), "details_fragment")

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
