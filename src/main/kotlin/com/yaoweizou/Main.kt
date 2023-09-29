package com.yaoweizou

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig
import org.jasypt.exceptions.EncryptionInitializationException
import org.jasypt.exceptions.EncryptionOperationNotPossibleException

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    val appVersion = "1.0.3"
    val primaryColor = Color(0xFF3DDC84)

    var input by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    var inputIsError by remember { mutableStateOf(false) }
    var passwordIsError by remember { mutableStateOf(false) }

    val focusRequesterInput = remember { FocusRequester() }
    val focusRequesterPassword = remember { FocusRequester() }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopAppBar(
                backgroundColor = primaryColor,
                contentColor = Color.Black,
                contentPadding = PaddingValues(start = 22.dp),
                content = { Text(text = "Jasypt Desktop", color = Color.Black) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    backgroundColor = Color.White,
                    cursorColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    focusedIndicatorColor = primaryColor,
                    errorLabelColor = Color.Red,
                    errorIndicatorColor = Color.Red,
                    errorLeadingIconColor = Color.Red
                ),
                modifier = Modifier.fillMaxWidth(0.8F).onPreviewKeyEvent { event ->
                    if (event.key == Key.Tab && event.type == KeyEventType.KeyUp) {
                        focusRequesterPassword.requestFocus()
                        return@onPreviewKeyEvent true
                    }
                    return@onPreviewKeyEvent false
                }.focusRequester(focusRequesterInput),
                label = { Text("Content") },
                value = input,
                onValueChange = {
                    if (!it.contains(" ") && !it.contains("\t")) {
                        input = it.trim()
                    }
                },
                isError = inputIsError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    backgroundColor = Color.White,
                    cursorColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    focusedIndicatorColor = primaryColor,
                    errorLabelColor = Color.Red,
                    errorIndicatorColor = Color.Red,
                    errorLeadingIconColor = Color.Red
                ),
                modifier = Modifier.fillMaxWidth(0.8F).focusRequester(focusRequesterPassword),
                label = { Text("Password") },
                value = password,
                onValueChange = {
                    if (!it.contains(" ") && !it.contains("\t")) {
                        password = it.trim()
                    }
                },
                isError = passwordIsError,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = "PBEWithMD5AndDES", color = Color(0xFFCCCCCC))
                Spacer(modifier = Modifier.width(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                    border = BorderStroke(1.dp, Color.Gray),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 25.dp, vertical = 10.dp),
                    onClick = {
                        if (input.isBlank() || password.isBlank()) {
                            inputIsError = false
                            passwordIsError = false
                            if (input.isBlank()) {
                                inputIsError = true
                            }
                            if (password.isBlank()) {
                                passwordIsError = true
                            }
                            return@OutlinedButton
                        }
                        inputIsError = false
                        passwordIsError = false
                        result = jasyptEncryptor(1, input, password)
                    }) {
                    Text("Encrypt")
                }

                Spacer(modifier = Modifier.width(22.dp))

                Button(colors = ButtonDefaults.buttonColors(
                    backgroundColor = primaryColor,
                    contentColor = Color.Black,
                ),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 25.dp, vertical = 10.dp),
                    onClick = {
                        if (input.isBlank() || password.isBlank()) {
                            inputIsError = false
                            passwordIsError = false
                            if (input.isBlank()) {
                                inputIsError = true
                            }
                            if (password.isBlank()) {
                                passwordIsError = true
                            }
                            return@Button
                        }
                        inputIsError = false
                        passwordIsError = false
                        result = jasyptEncryptor(0, input, password)
                    }) {
                    Text("Decrypt")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            TextField(colors = TextFieldDefaults.textFieldColors(
                textColor = Color.Black,
                backgroundColor = Color.White,
                cursorColor = primaryColor,
                focusedLabelColor = primaryColor,
                focusedIndicatorColor = primaryColor
            ),
                modifier = Modifier.fillMaxWidth(0.8F),
                readOnly = true,
                label = { Text("Result") },
                value = result,
                onValueChange = { })

            Spacer(modifier = Modifier.height(100.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = appVersion, color = Color(0xFFCCCCCC))
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

/**
 * @param type: 0: decrypt; 1: encrypt;
 */
fun jasyptEncryptor(type: Number, input: String, password: String): String {
    val config = SimpleStringPBEConfig()
    config.algorithm = "PBEWithMD5AndDES"
    config.setIvGeneratorClassName("org.jasypt.iv.NoIvGenerator")
    config.setPassword(password)
    val encryptor = StandardPBEStringEncryptor()
    encryptor.setConfig(config)
    return try {
        if (type == 0) {
            return encryptor.decrypt(input)
        }
        return encryptor.encrypt(input)
    } catch (e: EncryptionOperationNotPossibleException) {
        "Encryption/decryption information is incorrect. Please check the input."
    } catch (e: EncryptionInitializationException) {
        "Initialization failed. Please check the input."
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Jasypt Desktop",
        icon = BitmapPainter(useResource("icons/icon.png", ::loadImageBitmap))
    ) {
        App()
    }
}
