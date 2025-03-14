package nl.eduid.screens.resetpasswordconfirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import nl.eduid.ErrorData
import nl.eduid.R
import nl.eduid.ui.AlertDialogWithSingleButton
import nl.eduid.ui.EduIdTopAppBar
import nl.eduid.ui.PrimaryButton
import nl.eduid.ui.theme.ButtonBorderGrey
import nl.eduid.ui.theme.ButtonGreen
import nl.eduid.ui.theme.EduidAppAndroidTheme
import nl.eduid.ui.theme.TextBlack
import nl.eduid.ui.theme.TextGrey

@Composable
fun ResetPasswordConfirmScreen(
    viewModel: ResetPasswordConfirmViewModel,
    isAddPassword: Boolean,
    goBack: () -> Unit,
) = EduIdTopAppBar(
    onBackClicked = goBack,
) {
    val uiState by viewModel.uiState.observeAsState(UiState())

    ResetPasswordConfirmScreenContent(
        newPasswordInput = uiState.newPasswordInput,
        confirmPasswordInput = uiState.confirmPasswordInput,
        isAddPassword = isAddPassword,
        inProgress = uiState.inProgress,
        errorData = uiState.errorData,
        dismissError = viewModel::clearErrorState,
        isCompleted = uiState.isCompleted,
        onComplete = {
            viewModel.completedShown()
            goBack()
        },
        onNewPasswordChange = { viewModel.onNewPasswordInput(it) },
        onConfirmPasswordChange = {
            viewModel.onConfirmPasswordInput(it)
        },
        onResetPasswordClicked = viewModel::onResetPasswordClicked,
    ) { viewModel.onDeletePasswordClicked() }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ResetPasswordConfirmScreenContent(
    newPasswordInput: String = "",
    confirmPasswordInput: String = "",
    isAddPassword: Boolean = false,
    inProgress: Boolean = false,
    errorData: ErrorData? = null,
    dismissError: () -> Unit = {},
    isCompleted: Unit? = null,
    onComplete: () -> Unit = {},
    onNewPasswordChange: (newValue: String) -> Unit = {},
    onConfirmPasswordChange: (newValue: String) -> Unit = {},
    onResetPasswordClicked: () -> Unit = {},
    onDeletePasswordClicked: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var processing by rememberSaveable { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current
    if (processing && isCompleted != null && errorData == null) {
        LaunchedEffect(owner) {
            processing = false
            onComplete()
        }
    }

    if (errorData != null) {
        AlertDialogWithSingleButton(
            title = errorData.title,
            explanation = errorData.message,
            buttonLabel = stringResource(R.string.button_ok),
            onDismiss = {
                processing = false
                dismissError()
            }
        )
    }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (body, bottomColumn) = createRefs()
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .constrainAs(body) {
                    linkTo(parent.top, bottomColumn.top, bias = 0F)
                }
                .verticalScroll(rememberScrollState())

        ) {
            Spacer(Modifier.height(36.dp))
            Text(
                style = MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.Start,
                    color = ButtonGreen
                ),
                text = if (isAddPassword) {
                    stringResource(R.string.reset_password_add_title)
                } else {
                    stringResource(R.string.reset_password_change_title)
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(Modifier.height(32.dp))

            if (inProgress) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
            Text(
                style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Start),
                text = stringResource(R.string.reset_password_confirm_subtitle),
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = newPasswordInput,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                onValueChange = { onNewPasswordChange(it) },
                label = { Text(stringResource(R.string.reset_password_password_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = confirmPasswordInput,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                onValueChange = { onConfirmPasswordChange(it) },
                label = { Text(stringResource(R.string.reset_password_repeat_password_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                text = if (isAddPassword) {
                    stringResource(R.string.button_add_password)
                } else {
                    stringResource(R.string.button_reset_password)
                },
                enabled = !inProgress,
                onClick = {
                    processing = true
                    onResetPasswordClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(Modifier.height(30.dp))
            if (!isAddPassword) {
                Divider(color = TextBlack, thickness = 1.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    style = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Start,
                        color = ButtonGreen
                    ),
                    text = stringResource(R.string.reset_password_confirm_second_title),
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Start),
                    text = stringResource(R.string.reset_password_confirm_second_subtitle),
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.button_delete_password),
                    enabled = !inProgress,
                    onClick = {
                        processing = true
                        onDeletePasswordClicked()
                    },
                    buttonBackgroundColor = Color.Transparent,
                    buttonTextColor = TextGrey,
                    buttonBorderColor = ButtonBorderGrey,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewResetPasswordConfirmScreenContent() = EduidAppAndroidTheme {
    ResetPasswordConfirmScreenContent(
    )
}