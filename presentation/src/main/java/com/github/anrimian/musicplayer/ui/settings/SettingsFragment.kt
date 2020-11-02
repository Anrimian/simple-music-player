package com.github.anrimian.musicplayer.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentSettingsBinding
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsFragment
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsFragment
import com.github.anrimian.musicplayer.ui.settings.library.LibrarySettingsFragment
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment
import com.github.anrimian.musicplayer.ui.settings.themes.ThemeSettingsFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel

/**
 * Created on 19.10.2017.
 */
class SettingsFragment : Fragment(), FragmentLayerListener {
    
    private lateinit var viewBinding: FragmentSettingsBinding
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        val navigation = FragmentNavigation.from(parentFragmentManager)
        
        viewBinding.tvDisplay.setOnClickListener { navigation.addNewFragment(DisplaySettingsFragment()) }
        viewBinding.tvLibrary.setOnClickListener { navigation.addNewFragment(LibrarySettingsFragment()) }
        viewBinding.tvPlayer.setOnClickListener { navigation.addNewFragment(PlayerSettingsFragment()) }
        viewBinding.tvTheme.setOnClickListener { navigation.addNewFragment(ThemeSettingsFragment()) }
        viewBinding.tvHeadset.setOnClickListener { navigation.addNewFragment(HeadsetSettingsFragment()) }
        
        SlidrPanel.simpleSwipeBack(viewBinding.flContainer, this, toolbar::onStackFragmentSlided)

        viewBinding.composeView.setContent {
            DetailsContent(requireContext())
        }
    }

    override fun onFragmentMovedOnTop() {
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.subtitle = null
        toolbar.setTitleClickListener(null)
        toolbar.clearOptionsMenu()
    }

    @Composable
    fun DetailsContent(context: Context) {
        Column(verticalArrangement = Arrangement.Center) {
            SettingTextCell(context.getString(R.string.display))
            SettingTextCell(context.getString(R.string.library))
            SettingTextCell(context.getString(R.string.playing))
            SettingTextCell(context.getString(R.string.headset))
            SettingTextCell(context.getString(R.string.theme))
            Spacer(Modifier.preferredHeight(16.dp))
        }
    }
}

