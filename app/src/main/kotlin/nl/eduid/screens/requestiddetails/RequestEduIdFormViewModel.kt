package nl.eduid.screens.requestiddetails

import android.content.Context
import android.content.res.Resources
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import nl.eduid.BuildConfig
import nl.eduid.R
import nl.eduid.di.model.CREATE_EMAIL_SENT
import nl.eduid.di.model.EMAIL_DOMAIN_FORBIDDEN
import nl.eduid.di.model.FAIL_EMAIL_IN_USE
import nl.eduid.di.model.RequestEduIdAccount
import nl.eduid.di.repository.EduIdRepository
import nl.eduid.ErrorData
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RequestEduIdFormViewModel @Inject constructor(
    private val eduIdRepo: EduIdRepository,
) : ViewModel() {
    val inputForm = MutableLiveData(InputForm())

    fun onEmailChange(newValue: String) {
        inputForm.value = inputForm.value?.copy(email = newValue)
    }

    fun onFirstNameChange(newValue: String) {
        inputForm.value = inputForm.value?.copy(firstName = newValue)
    }

    fun onLastNameChange(newValue: String) {
        inputForm.value = inputForm.value?.copy(lastName = newValue)
    }

    fun onTermsAccepted(newValue: Boolean) {
        inputForm.value = inputForm.value?.copy(termsAccepted = newValue)
    }

    fun dismissError() {
        inputForm.value = inputForm.value?.copy(errorData = null)
    }

    fun requestNewEduIdAccount(context: Context): Job {
        return viewModelScope.launch {
            val inputData = inputForm.value ?: return@launch
            val relyingPartClientId = getClientIdFromOAuthConfig(context.resources)
            inputForm.postValue(inputData.copy(isProcessing = true, requestComplete = false))
            val responseStatus = eduIdRepo.requestEnroll(
                RequestEduIdAccount(
                    email = inputData.email,
                    givenName = inputData.firstName,
                    familyName = inputData.lastName,
                    relyingPartClientId = relyingPartClientId
                )
            )
            val newData = when (responseStatus) {
                CREATE_EMAIL_SENT -> {
                    inputData.copy(isProcessing = false, requestComplete = true)
                }
                FAIL_EMAIL_IN_USE -> {
                    inputData.copy(
                        isProcessing = false, errorData = ErrorData(
                            title = "Email in use",
                            message = "There already is an account registered for the email ${inputData.email}."
                        )
                    )
                }
                EMAIL_DOMAIN_FORBIDDEN -> {
                    inputData.copy(
                        isProcessing = false, errorData = ErrorData(
                            title = "Email domain forbidden",
                            message = "The email domain used in ${inputData.email} is not allowed."
                        )
                    )
                }
                else -> {
                    inputData.copy(
                        isProcessing = false, errorData = ErrorData(
                            title = "Unknown status",
                            message = "Could not create eduid account for email ${inputData.email}"
                        )
                    )
                }
            }

            inputForm.postValue(newData)
        }
    }

    private fun getClientIdFromOAuthConfig(resources: Resources): String {
        val source =
            resources.openRawResource(R.raw.auth_config).bufferedReader().use { it.readText() }
        return try {
            JSONObject(source).get("client_id").toString()
        } catch (e: IOException) {
            Timber.e(e, "Failed to parse configurations")
            BuildConfig.CLIENT_ID
        }
    }

}

data class InputForm(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val termsAccepted: Boolean = false,
    val isProcessing: Boolean = false,
    val requestComplete: Boolean = false,
    val errorData: ErrorData? = null,
) {
    val emailValid: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    val isFormValid: Boolean
        get() = (emailValid && firstName.isNotEmpty() && lastName.isNotEmpty() && termsAccepted)
}

