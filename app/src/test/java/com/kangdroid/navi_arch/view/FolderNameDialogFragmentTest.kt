package com.kangdroid.navi_arch.view

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.DialogAddFolderBinding
import com.kangdroid.navi_arch.viewmodel.ViewModelTestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FolderNameDialogFragmentTest {
    @Test
    fun is_viewBinding_onCreate_works_well() {
        val scenario: FragmentScenario<FolderNameDialog> = launchFragmentInContainer(
            themeResId = R.style.Theme_NaviArch
        ) {
            FolderNameDialog {}
        }.apply {
            moveToState(Lifecycle.State.STARTED)
        }

        scenario.onFragment {
            val dialogAddFolderBinding: DialogAddFolderBinding? =
                ViewModelTestHelper.getFields("_dialogBinding", it)

            assertThat(dialogAddFolderBinding).isNotEqualTo(null)
        }
    }

    @Test
    fun is_cancelCreateFolder_works_well() {
        val scenario: FragmentScenario<FolderNameDialog> = launchFragmentInContainer(
            themeResId = R.style.Theme_NaviArch
        ) {
            FolderNameDialog {}
        }.apply {
            moveToState(Lifecycle.State.STARTED)
        }

        scenario.onFragment {
            val dialogAddFolderBinding: DialogAddFolderBinding =
                ViewModelTestHelper.getFields("_dialogBinding", it)

            dialogAddFolderBinding.cancelCreateFolder.performClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }
        }
    }

    @Test
    fun is_createFolder_works_well() {
        var tmpString: String = "-1"
        val targetFolderName: String = "Hello, World!"
        val scenario: FragmentScenario<FolderNameDialog> = launchFragmentInContainer(
            themeResId = R.style.Theme_NaviArch
        ) {
            FolderNameDialog {
                tmpString = it
            }
        }.apply {
            moveToState(Lifecycle.State.STARTED)
        }

        scenario.onFragment {
            val dialogAddFolderBinding: DialogAddFolderBinding =
                ViewModelTestHelper.getFields("_dialogBinding", it)

            // Set Name first
            dialogAddFolderBinding.folderName.setText(targetFolderName)

            dialogAddFolderBinding.confirmCreateFolder.performClick().also { clickResult ->
                assertThat(clickResult).isEqualTo(true)
            }

            assertThat(tmpString).isEqualTo(targetFolderName)
        }
    }
}