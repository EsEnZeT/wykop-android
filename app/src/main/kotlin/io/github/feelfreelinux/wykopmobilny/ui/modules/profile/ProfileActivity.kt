package io.github.feelfreelinux.wykopmobilny.ui.modules.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.feelfreelinux.wykopmobilny.R
import io.github.feelfreelinux.wykopmobilny.api.patrons.PatronsApi
import io.github.feelfreelinux.wykopmobilny.base.BaseActivity
import io.github.feelfreelinux.wykopmobilny.databinding.ActivityProfileBinding
import io.github.feelfreelinux.wykopmobilny.databinding.BadgeListItemBinding
import io.github.feelfreelinux.wykopmobilny.databinding.BadgesBottomsheetBinding
import io.github.feelfreelinux.wykopmobilny.models.dataclass.drawBadge
import io.github.feelfreelinux.wykopmobilny.models.fragments.DataFragment
import io.github.feelfreelinux.wykopmobilny.models.fragments.getDataFragmentInstance
import io.github.feelfreelinux.wykopmobilny.models.pojo.apiv2.models.ObserveStateResponse
import io.github.feelfreelinux.wykopmobilny.models.pojo.apiv2.models.ProfileResponse
import io.github.feelfreelinux.wykopmobilny.models.pojo.apiv2.patrons.PatronBadge
import io.github.feelfreelinux.wykopmobilny.models.pojo.apiv2.responses.BadgeResponse
import io.github.feelfreelinux.wykopmobilny.ui.modules.NewNavigatorApi
import io.github.feelfreelinux.wykopmobilny.utils.api.getGenderStripResource
import io.github.feelfreelinux.wykopmobilny.utils.api.getGroupColor
import io.github.feelfreelinux.wykopmobilny.utils.loadImage
import io.github.feelfreelinux.wykopmobilny.utils.toDurationPrettyDate
import io.github.feelfreelinux.wykopmobilny.utils.toPrettyDate
import io.github.feelfreelinux.wykopmobilny.utils.usermanager.UserManagerApi
import io.github.feelfreelinux.wykopmobilny.utils.viewBinding
import javax.inject.Inject

class ProfileActivity : BaseActivity(), ProfileView {

    companion object {
        const val EXTRA_USERNAME = "EXTRA_USERNAME"
        const val DATA_FRAGMENT_TAG = "PROFILE_DATAFRAGMENT"

        fun createIntent(context: Context, username: String) =
            Intent(context, ProfileActivity::class.java).apply {
                putExtra(EXTRA_USERNAME, username)
            }
    }

    @Inject
    lateinit var navigator: NewNavigatorApi

    @Inject
    lateinit var presenter: ProfilePresenter

    @Inject
    lateinit var userManagerApi: UserManagerApi

    @Inject
    lateinit var patronsApi: PatronsApi

    private val binding by viewBinding(ActivityProfileBinding::inflate)

    val username by lazy { intent.getStringExtra(EXTRA_USERNAME)!! }
    override val enableSwipeBackLayout: Boolean = true
    private var observeStateResponse: ObserveStateResponse? = null
    private lateinit var badgesDialogListener: (List<BadgeResponse>) -> Unit
    private val pagerAdapter by lazy { ProfilePagerAdapter(resources, supportFragmentManager) }

    lateinit var dataFragment: DataFragment<ProfileResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataFragment = supportFragmentManager.getDataFragmentInstance(DATA_FRAGMENT_TAG)
        setSupportActionBar(binding.toolbar.toolbar)
        presenter.subscribe(this)
        presenter.userName = username
        binding.fab.setOnClickListener {
            navigator.openAddEntryActivity(null, "@$username: ")
        }
        binding.fab.isVisible = userManagerApi.isUserAuthorized()

        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(true)
        }
        supportActionBar?.title = null

        binding.tabLayout.setupWithViewPager(binding.pager)
        if (dataFragment.data != null) {
            showProfile(dataFragment.data!!)
        } else {
            presenter.loadData()
            binding.loadingView.isVisible = true
            binding.appBarLayout.isVisible = true
        }
    }

    fun getBadgeFor(nick: String): PatronBadge? {
        return try {
            patronsApi.patrons.firstOrNull { it.username == nick }?.badge
        } catch (e: Throwable) {
            null
        }
    }

    override fun showProfile(profileResponse: ProfileResponse) {
        dataFragment.data = profileResponse
        binding.pager.offscreenPageLimit = 2
        binding.pager.adapter = pagerAdapter
        getBadgeFor(profileResponse.login)?.drawBadge(binding.patronBadgeTextView)
        binding.tabLayout.setupWithViewPager(binding.pager)
        binding.profilePicture.loadImage(profileResponse.avatar)
        binding.signup.text = profileResponse.signupAt.toDurationPrettyDate()
        binding.nickname.text = profileResponse.login
        binding.nickname.setTextColor(getGroupColor(profileResponse.color))
        binding.loadingView.isVisible = false
        binding.description.isVisible = profileResponse.description != null
        profileResponse.description?.let {
            binding.description.isVisible = true
            binding.description.text = profileResponse.description
        }
        profileResponse.isObserved?.let {
            observeStateResponse = ObserveStateResponse(profileResponse.isObserved, profileResponse.isBlocked!!)
            invalidateOptionsMenu()
        }
        if (profileResponse.followers != 0) {
            binding.followers.isVisible = true
            binding.followers.text = getString(R.string.followers, dataFragment.data!!.followers)
        }
        if (profileResponse.rank != 0) {
            binding.rank.isVisible = true
            binding.rank.text = "#${profileResponse.rank}"
            binding.rank.setBackgroundColor(getGroupColor(profileResponse.color))
        }
        profileResponse.sex?.let {
            binding.genderStripImageView.isVisible = true
            binding.genderStripImageView.setBackgroundResource(getGenderStripResource(profileResponse.sex))
        }

        profileResponse.ban?.apply {
            if (reason != null && date != null) {
                binding.banTextView.isVisible = true
                binding.banTextView.text = "Użytkownik zbanowany do $date za $reason"
            }
        }
        binding.backgroundImg.isVisible = true
        profileResponse.background?.let {
            binding.backgroundImg.loadImage(profileResponse.background)
            binding.toolbar.toolbar.setBackgroundResource(R.drawable.gradient_toolbar_up)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        if (userManagerApi.isUserAuthorized() && userManagerApi.getUserCredentials()!!.login != username) {
            menu.findItem(R.id.pw).isVisible = true
            observeStateResponse?.apply {
                menu.apply {
                    findItem(R.id.unobserve_profile).isVisible = isObserved
                    findItem(R.id.observe_profile).isVisible = !isObserved
                    findItem(R.id.block).isVisible = !isBlocked
                    findItem(R.id.unblock).isVisible = isBlocked
                }
            }
        }
        return true
    }

    override fun showButtons(observeState: ObserveStateResponse) {
        observeStateResponse = observeState
        invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pw -> dataFragment.data?.let { navigator.openConversationListActivity(dataFragment.data!!.login) }
            R.id.unblock -> {
                presenter.markUnblocked()
            }
            R.id.block -> {
                presenter.markBlocked()
            }
            R.id.observe_profile -> {
                presenter.markObserved()
            }
            R.id.unobserve_profile -> {
                presenter.markUnobserved()
            }
            R.id.badges -> {
                showBadgesDialog()
            }
            R.id.report -> {
                navigator.openReportScreen(dataFragment.data!!.violationUrl!!)
            }
            android.R.id.home -> finish()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showBadgesDialog() {
        val dialog = BottomSheetDialog(this)
        val badgesDialogView = BadgesBottomsheetBinding.inflate(layoutInflater)
        dialog.setContentView(badgesDialogView.root)
        badgesDialogListener = {
            if (dialog.isShowing) {
                badgesDialogView.loadingView.isVisible = false
                for (badge in it) {
                    val item = BadgeListItemBinding.inflate(layoutInflater)
                    item.description.text = badge.description
                    item.date.text = badge.date.toPrettyDate()
                    item.badgeTitle.text = badge.name
                    item.badgeImg.loadImage(badge.icon)
                    badgesDialogView.badgesList.addView(item.root)
                }
            }
        }
        dialog.show()
        presenter.getBadges()
    }

    override fun showBadges(badges: List<BadgeResponse>) = badgesDialogListener(badges)

    override fun onDestroy() {
        presenter.unsubscribe()
        super.onDestroy()
    }
}
