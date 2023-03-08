package org.telegram.ui.Components.voip;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;

import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.Theme;

/**
 * Created by grishka on 29.09.2017.
 */

public class DarkTheme{

	public static int getColor(String key){
		switch(key){
			case "avatar_subtitleInProfilePink":
				return 0xFF8A8A8A;
			case "chat_emojiPanelTrendingDescription":
				return 0xFF717171;
			case "chat_inFileBackground":
				return 0xFF5D6F80;
			case "chat_emojiPanelIconSelected":
				return 0xFF5598DB;
			case "actionBarActionModeDefaultSelector":
				return 0x7A0F1923;
			case "chats_menuItemIcon":
				return 0xFF828282;
			case "chat_inTimeText":
				return 0xD98091A0;
			case "windowBackgroundGray":
				return 0xFF0D0D0D;
			case "windowBackgroundWhiteGreenText2":
				return 0xFF42C366;
			case "chat_emojiPanelBackspace":
				return 0xFF727272;
			case "chat_inBubble":
				return 0xFF253442;
			case "chat_outFileInfoSelectedText":
				return 0xFFFFFFFF;
			case "chat_outLoaderSelected":
				return 0xFFFFFFFF;
			case "chat_emojiPanelIcon":
				return 0xFF717171;
			case "chat_selectedBackground":
				return 0x4C0F99ED;
			case "chats_pinnedIcon":
				return 0xFF787878;
			case "player_actionBarTitle":
				return 0xFFE7E7E7;
			case "chat_muteIcon":
				return 0xFF7E7E7E;
			case "chat_mediaMenu":
				return 0xFFFFFFFF;
			case "chat_addContact":
				return 0xFF55A3DB;
			case "chat_outMenu":
				return 0xFF6D9ACE;
			case "actionBarActionModeDefault":
				return 0xFF253442;
			case "chat_emojiPanelShadowLine":
				return 0x0EFFFFFF;
			case "dialogBackground":
				return 0xFF212426;
			case "chat_inPreviewInstantText":
				return 0xFF55A2DB;
			case "chat_outVoiceSeekbarSelected":
				return 0xFFEBF3FF;
			case "chat_outForwardedNameText":
				return 0xFFD1EBFF;
			case "chat_outFileProgressSelected":
				return 0xFFFFFFFF;
			case "player_progressBackground":
				return 0x8A000000;
			case "avatar_actionBarSelectorRed":
				return 0xFF495154;
			case "player_button":
				return 0xFF868686;
			case "chat_inVoiceSeekbar":
				return 0xFF5D6F80;
			case "switchThumb":
				return 0xFF3C3C3C;
			case "chats_tabletSelectedOverlay":
				return 0x0FFFFFFF;
			case "chats_menuItemText":
				return 0xFFF0F0F0;
			case "chat_outFileNameText":
				return 0xFFD2EBFF;
			case "divider":
				return 0x17FFFFFF;
			case "chat_outViews":
				return 0xFF82B2DC;
			case "avatar_actionBarSelectorBlue":
				return 0xFF495054;
			case "chats_actionMessage":
				return 0xFF5491C6;
			case "groupcreate_spanBackground":
				return 0xFF282E33;
			case "chat_messageTextIn":
				return 0xFFFAFAFA;
			case "chat_serviceBackgroundSelected":
				return 0x60495154;
			case "inappPlayerBackground":
				return 0xD82B2B2B;
			case "chat_topPanelLine":
				return 0xFF5680A9;
			case "chat_outFileInfoText":
				return 0xFFAACFEE;
			case "chat_unreadMessagesStartArrowIcon":
				return 0xFF5A6B7A;
			case "chat_outAudioProgress":
				return 0xFF3873A4;
			case "chat_outBubbleShadow":
				return 0xFF000000;
			case "chat_inMenuSelected":
				return 0x82A9CFEE;
			case "chat_inContactIcon":
				return 0xFF253542;
			case "chat_messageTextOut":
				return 0xFFFAFAFA;
			case "chat_outAudioTitleText":
				return 0xFFD1EBFF;
			case "inappPlayerPerformer":
				return 0xFFFAFAFA;
			case "actionBarActionModeDefaultTop":
				return 0xA4000000;
			case "avatar_subtitleInProfileCyan":
				return 0xFF8A8A8A;
			case "profile_actionBackground":
				return 0xFF383E42;
			case "chat_outSentClockSelected":
				return 0xFFFFFFFF;
			case "avatar_nameInMessageGreen":
				return 0xFF6CB55B;
			case "chat_outAudioSeekbarFill":
				return 0xFFC4E1F7;
			case "chat_inReplyNameText":
				return 0xFF55A2DB;
			case "chat_messagePanelIcons":
				return 0xFF696969;
			case "graySection":
				return 0xFF222222;
			case "avatar_backgroundActionBarViolet":
				return 0xFF212426;
			case "chat_outPreviewInstantText":
				return 0xFFD1EBFF;
			case "chat_emojiPanelTrendingTitle":
				return 0xFFF4F4F4;
			case "chat_inFileInfoSelectedText":
				return 0xFFA9CFEE;
			case "avatar_subtitleInProfileRed":
				return 0xFF8A8A8A;
			case "chat_outLocationIcon":
				return 0xFF669ABF;
			case "chat_inAudioPerfomerText":
				return 0xFF798897;
			case "chats_attachMessage":
				return 0xFF5491C6;
			case "chat_messageLinkIn":
				return 0xFF56A3DB;
			case "chats_unreadCounter":
				return 0xFF2794DE;
			case "windowBackgroundWhiteGrayText":
				return 0xFF656565;
			case "windowBackgroundWhiteGrayText3":
				return 0xFF707070;
			case "actionBarDefaultSubmenuBackground":
				return 0xFB1E2022;
			case "chat_outSentCheckSelected":
				return 0xFFFFFFFF;
			case "chat_outTimeSelectedText":
				return 0xFFFFFFFF;
			case "chats_secretIcon":
				return 0xFF71D756;
			case "dialogIcon":
				return 0xFF7A848D;
			case "chat_outAudioPerfomerText":
				return 0xFF94C0E2;
			case "chats_pinnedOverlay":
				return 0x09FFFFFF;
			case "chat_outContactIcon":
				return 0xFFACCDFF;
			case "windowBackgroundWhiteBlueHeader":
				return 0xFF69ABF3;
			case "actionBarDefaultSelector":
				return 0xFF495154;
			case "chat_emojiPanelEmptyText":
				return 0xFF5D5D5D;
			case "chat_inViews":
				return 0xFF798997;
			case "listSelector":
				return 0x2E000000;
			case "chat_messagePanelBackground":
				return 0xFF1E1E1E;
			case "chats_secretName":
				return 0xFF71D756;
			case "chat_inReplyLine":
				return 0xFF54A2DB;
			case "actionBarDefaultSubtitle":
				return 0xFF8F8F8F;
			case "switchThumbChecked":
				return 0xFF3078A8;
			case "chat_inReplyMessageText":
				return 0xFFFFFFFF;
			case "avatar_actionBarSelectorGreen":
				return 0xFF495154;
			case "chat_inAudioTitleText":
				return 0xFF56A3DB;
			case "chat_inAudioDurationSelectedText":
				return 0xFFA9CFEE;
			case "chat_outSentClock":
				return 0xFF82B2DC;
			case "actionBarDefault":
				return 0xFF242728;
			case "chat_goDownButton":
				return 0xFF4D4D4D;
			case "chat_inAudioSelectedProgress":
				return 0xFF1C4163;
			case "profile_actionPressedBackground":
				return 0xFF495054;
			case "chat_outContactPhoneText":
				return 0xFFB6DFFF;
			case "chat_inVenueInfoText":
				return 0xFF5D6F80;
			case "chat_outAudioDurationText":
				return 0xFFD1EBFF;
			case "windowBackgroundWhiteLinkText":
				return 0xFF3D92D2;
			case "chat_outSiteNameText":
				return 0xFFD1EBFF;
			case "chat_inBubbleSelected":
				return 0xFF1C4063;
			case "chats_date":
				return 0xFF5E5E5E;
			case "chat_outFileProgress":
				return 0xFF72A5D0;
			case "chat_outBubbleSelected":
				return 0xFF2C83CB;
			case "progressCircle":
				return 0xFF364044;
			case "chats_unreadCounterMuted":
				return 0xFF444444;
			case "stickers_menu":
				return 0xFF4D5053;
			case "chat_outAudioSeekbarSelected":
				return 0xFFFFFFFF;
			case "chat_inSiteNameText":
				return 0xFF55A2DB;
			case "chat_inFileProgressSelected":
				return 0xFFA6CFEE;
			case "chat_topPanelMessage":
				return 0xFF6A6A6A;
			case "chat_outVoiceSeekbar":
				return 0xFF72A5D0;
			case "chat_topPanelBackground":
				return 0xFA1C1C1C;
			case "chat_outVenueInfoSelectedText":
				return 0xFFFFFFFF;
			case "chats_menuTopShadow":
				return 0xFF101010;
			case "dialogTextBlack":
				return 0xFFF9F9F9;
			case "player_actionBarItems":
				return 0xFFFFFFFF;
			case "files_folderIcon":
				return 0xFFA6A6A6;
			case "chat_inReplyMediaMessageSelectedText":
				return 0xFF6DA8DF;
			case "chat_inViewsSelected":
				return 0xFFA9CFEE;
			case "chat_outAudioDurationSelectedText":
				return 0xFFFFFFFF;
			case "avatar_backgroundActionBarGreen":
				return 0xFF212426;
			case "profile_verifiedCheck":
				return 0xFFFFFFFF;
			case "chat_outViewsSelected":
				return 0xFFFFFFFF;
			case "switchTrackChecked":
				return 0xFF164A72;
			case "chat_serviceBackground":
				return 0x6628323D;
			case "windowBackgroundWhiteGrayText2":
				return 0xFF797979;
			case "profile_actionIcon":
				return 0xFFFFFFFF;
			case "chat_secretChatStatusText":
				return 0xFF686868;
			case "chat_emojiPanelBackground":
				return 0xFF232323;
			case "chat_inPreviewLine":
				return 0xFF54A2DB;
			case "chat_unreadMessagesStartBackground":
				return 0xFF253442;
			case "avatar_backgroundActionBarBlue":
				return 0xFF212426;
			case "chat_inViaBotNameText":
				return 0xFF55A2DB;
			case "avatar_actionBarSelectorCyan":
				return 0xFF495154;
			case "avatar_nameInMessageOrange":
				return 0xFFDC8859;
			case "windowBackgroundWhiteGrayText4":
				return 0xFF6E6E6E;
			case "files_folderIconBackground":
				return 0xFF303030;
			case "profile_verifiedBackground":
				return 0xFF51CBF8;
			case "chat_outFileBackground":
				return 0xFF72A5D0;
			case "chat_inLoaderPhoto":
				return 0xFF243442;
			case "dialogTextLink":
				return 0xFF3984D1;
			case "chat_inForwardedNameText":
				return 0xFF55A2DB;
			case "chat_inSentClock":
				return 0xFF5D6F80;
			case "chat_inAudioSeekbarSelected":
				return 0xFFA9CFEE;
			case "chats_name":
				return 0xFFE6E6E6;
			case "chats_nameMessage":
				return 0xFF4D87B6;
			case "key_chats_menuTopShadow":
				return 0x000C0C0C;
			case "windowBackgroundWhite":
				return 0xFF171819;
			case "chat_outBubble":
				return 0xFF3872A4;
			case "chats_menuBackground":
				return 0xFF1D2023;
			case "chat_messagePanelHint":
				return 0xFF4C4C4C;
			case "chat_replyPanelLine":
				return 0xFF1D1D1D;
			case "chat_inReplyMediaMessageText":
				return 0xFF798897;
			case "chat_outReplyMediaMessageText":
				return 0xFFD1EBFF;
			case "avatar_backgroundActionBarPink":
				return 0xFF212426;
			case "chat_outLoader":
				return 0xFF8EBFE8;
			case "chat_outReplyNameText":
				return 0xFFD1EBFF;
			case "avatar_subtitleInProfileViolet":
				return 0xFF8A8A8A;
			case "chat_outAudioSelectedProgress":
				return 0xFF2782CB;
			case "chat_inSentClockSelected":
				return 0xFFA9D0EE;
			case "chat_inBubbleShadow":
				return 0xFF000000;
			case "chat_inFileInfoText":
				return 0xFF798997;
			case "windowBackgroundWhiteGrayIcon":
				return 0xFF828282;
			case "chat_inAudioSeekbar":
				return 0xFF516170;
			case "chat_inContactPhoneText":
				return 0xFF798897;
			case "avatar_backgroundInProfileBlue":
				return 0xFF549CDD;
			case "chat_outInstantSelected":
				return 0xFFFFFFFF;
			case "chat_outAudioSeekbar":
				return 0x9672A5D0;
			case "windowBackgroundWhiteRedText5":
				return 0xFFFF4C56;
			case "avatar_actionBarSelectorViolet":
				return 0xFF495154;
			case "chats_menuPhone":
				return 0x60FFFFFF;
			case "chat_outVoiceSeekbarFill":
				return 0xFFC4E1F7;
			case "actionBarDefaultSubmenuItem":
				return 0xFFF5F5F5;
			case "chat_outPreviewLine":
				return 0xFFD1EBFF;
			case "chats_sentCheck":
				return 0xFF5EA4E0;
			case "chat_inMenu":
				return 0x795C6F80;
			case "chats_sentClock":
				return 0xFF6082BD;
			case "chat_messageLinkOut":
				return 0xFFB6DEFF;
			case "chat_unreadMessagesStartText":
				return 0xDAFFFFFF;
			case "inappPlayerClose":
				return 0xFF585858;
			case "chat_inAudioProgress":
				return 0xFF253542;
			case "chat_outFileBackgroundSelected":
				return 0xFFFFFFFF;
			case "chat_outInstant":
				return 0xFFB6DFFF;
			case "chat_outReplyMessageText":
				return 0xFFFFFFFF;
			case "chat_outContactBackground":
				return 0xFF5985C2;
			case "chat_inAudioDurationText":
				return 0xFF7A8897;
			case "listSelectorSDK21":
				return 0x11FFFFFF;
			case "chat_goDownButtonIcon":
				return 0xFFE4E4E4;
			case "windowBackgroundWhiteBlueText4":
				return 0xFF4A8FCD;
			case "chat_inContactNameText":
				return 0xFF56A3DB;
			case "chat_topPanelTitle":
				return 0xFF55A3DB;
			case "avatar_actionBarSelectorPink":
				return 0xFF495154;
			case "chat_outContactNameText":
				return 0xFFD1EBFF;
			case "player_actionBarSubtitle":
				return 0xFF5F5F5F;
			case "chat_wallpaper":
				return 0xFF131617;
			case "chat_emojiPanelStickerPackSelector":
				return 0x0CFAFEFF;
			case "chats_menuPhoneCats":
				return 0xFF8E8E8E;
			case "chat_reportSpam":
				return 0xFFE96461;
			case "avatar_subtitleInProfileGreen":
				return 0xFF8A8A8A;
			case "inappPlayerTitle":
				return 0xFF9C9C9C;
			case "chat_outViaBotNameText":
				return 0xFFD1EBFF;
			case "avatar_backgroundActionBarRed":
				return 0xFF212426;
			case "windowBackgroundWhiteValueText":
				return 0xFF459DE1;
			case "avatar_backgroundActionBarOrange":
				return 0xFF212426;
			case "chat_inFileBackgroundSelected":
				return 0xFFFFFFFF;
			case "avatar_actionBarSelectorOrange":
				return 0xFF495154;
			case "chat_inVenueInfoSelectedText":
				return 0xFFA9CFEE;
			case "actionBarActionModeDefaultIcon":
				return 0xFFFFFFFF;
			case "chats_message":
				return 0xFF686868;
			case "avatar_subtitleInProfileBlue":
				return 0xFF8A8A8A;
			case "chat_outVenueNameText":
				return 0xFFD1EBFF;
			case "emptyListPlaceholder":
				return 0xFF515151;
			case "chat_inFileProgress":
				return 0xFF5D6F80;
			case "chats_muteIcon":
				return 0xFF5B5B5B;
			case "groupcreate_spanText":
				return 0xFFF5F5F5;
			case "windowBackgroundWhiteBlackText":
				return 0xFFF2F2F2;
			case "windowBackgroundWhiteBlueText":
				return 0xFF4295D9;
			case "chat_outReplyMediaMessageSelectedText":
				return 0xFFFFFFFF;
			case "avatar_backgroundActionBarCyan":
				return 0xFF212426;
			case "chat_topPanelClose":
				return 0xFF555555;
			case "chat_outSentCheck":
				return 0xFF97C3EA;
			case "chat_outMenuSelected":
				return 0xFFFFFFFF;
			case "chat_messagePanelText":
				return 0xFFEEEEEE;
			case "chat_outReplyLine":
				return 0xFFD1EBFF;
			case "dialogBackgroundGray":
				return 0xFF4B555D;
			case "dialogButtonSelector":
				return 0x14FFFFFF;
			case "chat_outVenueInfoText":
				return 0xFFB6DFFF;
			case "chat_outTimeText":
				return 0xD6A8CFEE;
			case "chat_inTimeSelectedText":
				return 0xFFAACFEE;
			case "switchTrack":
				return 0xFF2B2B2B;
			case "avatar_subtitleInProfileOrange":
				return 0xFF8A8A8A;
		}
		FileLog.w("returning color for key "+key+" from current theme");
		return Theme.getColor(key);
	}

	public static Drawable getThemedDrawable(Context context, int resId, String key) {
		Drawable drawable = context.getResources().getDrawable(resId).mutate();
		drawable.setColorFilter(new PorterDuffColorFilter(getColor(key), PorterDuff.Mode.MULTIPLY));
		return drawable;
	}

}
