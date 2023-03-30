package nl.eduid.screens.oauth

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class OAuthUiStages(
    val isAuthorizationLaunched: Boolean = false,
    val isFetchingToken: Boolean = false,
) : Parcelable