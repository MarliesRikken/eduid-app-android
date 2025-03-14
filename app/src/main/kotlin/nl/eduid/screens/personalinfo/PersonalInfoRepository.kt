package nl.eduid.screens.personalinfo

import android.annotation.SuppressLint
import android.os.Environment
import android.util.AtomicFile
import androidx.core.util.writeText
import nl.eduid.di.api.EduIdApi
import nl.eduid.di.model.ConfirmDeactivationCode
import nl.eduid.di.model.ConfirmPhoneCode
import nl.eduid.di.model.DeleteServiceRequest
import nl.eduid.di.model.EnrollResponse
import nl.eduid.di.model.LinkedAccount
import nl.eduid.di.model.RequestPhoneCode
import nl.eduid.di.model.SelfAssertedName
import nl.eduid.di.model.Token
import nl.eduid.di.model.TokenResponse
import nl.eduid.di.model.UserDetails
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar

class PersonalInfoRepository(private val eduIdApi: EduIdApi) {

    suspend fun getUserDetails(): UserDetails? = try {
        val response = eduIdApi.getUserDetails()
        if (response.isSuccessful) {
            response.body()
        } else {
            Timber.w(
                "User details not available [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to retrieve user details")
        null
    }

    suspend fun startEnrollment(): EnrollResponse? = try {
        val response = eduIdApi.startEnrollment()
        if (response.isSuccessful) {
            response.body()
        } else {
            Timber.w(
                "Failed to start enrollment [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to start enrollment")
        null
    }

    suspend fun removeService(serviceId: String): UserDetails? = try {
        val tokens = getTokensForUser()
        val tokensForService = tokens?.filter { token ->
            token.clientId == serviceId && token.scopes?.any { scope ->
                scope.name != "openid" && scope.hasValidDescription()
            } ?: false
        }
        val tokensRequest = tokensForService?.map { serviceToken ->
            Token(serviceToken.id, serviceToken.type)
        } ?: emptyList()

        val response = eduIdApi.removeService(
            DeleteServiceRequest(
                serviceProviderEntityId = serviceId, tokens = tokensRequest
            )
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Timber.w(
                "Failed to remove connection for [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to remove service with id $serviceId")
        null
    }

    private suspend fun getTokensForUser(): List<TokenResponse>? = try {
        val tokenResponse = eduIdApi.getTokens()
        if (tokenResponse.isSuccessful) {
            tokenResponse.body()
        } else {
            Timber.w(
                "Failed to remove connection for [${tokenResponse.code()}/${tokenResponse.message()}]${
                    tokenResponse.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to get tokens granted for current user")
        null
    }

    suspend fun removeConnection(linkedAccount: LinkedAccount): UserDetails? = try {
        val response = eduIdApi.removeConnection(linkedAccount)
        if (response.isSuccessful) {
            response.body()
        } else {
            Timber.w(
                "Failed to remove connection for [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to remove connection for ${linkedAccount.institutionIdentifier}")
        null
    }

    suspend fun updateName(selfAssertedName: SelfAssertedName): UserDetails? = try {
        val response = eduIdApi.updateName(selfAssertedName)
        if (response.isSuccessful) {
            response.body()
        } else {
            Timber.w(
                "Failed to update name [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed update name")
        null
    }

    suspend fun getInstitutionName(schac_home: String): String? = try {
        val response = eduIdApi.getInstitutionName(schac_home)
        if (response.isSuccessful) {
            response.body()?.displayNameEn
        } else {
            Timber.w(
                "Institution name lookup failed. [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to retrieve institution name")
        null
    }

    suspend fun getStartLinkAccount(): String? = try {
        val response = eduIdApi.getStartLinkAccount()
        if (response.isSuccessful) {
            response.body()?.url
        } else {
            Timber.w(
                "Failed to retrieve start link account URL: [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to retrieve start link account URL")
        null
    }

    @SuppressLint("SimpleDateFormat")
    suspend fun downloadPersonalData(): Boolean = try {
        val response = eduIdApi.getPersonalData()
        if (response.isSuccessful) {
            val personalDataJson = response.body()
            if (personalDataJson != null) {
                val downloadFolder =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val now = Calendar.getInstance().time
                val format = SimpleDateFormat("dMy")
                val timestamp = format.format(now)
                val file = AtomicFile(File(downloadFolder, "eduid_export_${timestamp}.json"))
                file.writeText(personalDataJson)
                true
            } else {
                false
            }
        } else {
            Timber.w(
                "Failed to get personal data [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            false
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to download and save personal data.")
        false
    }

    suspend fun deleteAccount() = try {
        val response = eduIdApi.deleteAccount()
        response.isSuccessful
    } catch (e: Exception) {
        Timber.e(e, "Failed to delete account")
        false
    }

    suspend fun resetPasswordLink(): UserDetails? = try {
        val response = eduIdApi.resetPasswordLink()
        if (response.isSuccessful) {
            response.body()
        } else {
            Timber.w(
                "Failed to send password link: [${response.code()}/${response.message()}]${
                    response.errorBody()?.string()
                }"
            )
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to send password link")
        null
    }

    suspend fun requestDeactivationForKnownPhone() = try {
        val response = eduIdApi.requestDeactivationForKnownPhone()
        response.isSuccessful
    } catch (e: Exception) {
        Timber.e(e, "Failed to request deactivation phone code")
        false
    }

    suspend fun requestPhoneCode(phoneNumber: String) = try {
        val response = eduIdApi.requestPhoneCode(RequestPhoneCode(phoneNumber))
        response.isSuccessful
    } catch (e: Exception) {
        Timber.e(e, "Failed to request phone code")
        false
    }

    suspend fun confirmPhoneCode(phoneCode: String) = try {
        val response = eduIdApi.confirmPhoneCode(ConfirmPhoneCode(phoneCode))
        response.isSuccessful
    } catch (e: Exception) {
        Timber.e(e, "Failed to verify phone code")
        false
    }

    suspend fun deactivateApp(phoneCode: String) = try {
        val response = eduIdApi.deactivateApp(ConfirmDeactivationCode(phoneCode))
        response.isSuccessful
    } catch (e: Exception) {
        Timber.e(e, "Failed to deactivate app")
        false
    }
}