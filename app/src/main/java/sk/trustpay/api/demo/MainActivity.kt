
package sk.trustpay.api.demo

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sk.trustpay.api.demo.databinding.ActivityMainBinding
import sk.trustpay.api.sdk.common.*
import sk.trustpay.api.sdk.dto.*
import sk.trustpay.api.sdk.methods.*
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tokenProvider: TokenProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenProvider = TokenProvider(DemoPaymentData.ProjectId, BuildConfig.SECRET_KEY)

        setUpPaymentMethods()
        setUpRandomData()

        val payButton = binding.button
        payButton.setOnClickListener{
            validateAndSetUpPayment()
        }
    }

    override fun onResume() {
        super.onResume()
        setUpRandomData()
    }

    private fun setUpPaymentMethods() {
        val adapterPaymentMethods =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("Wire", "Card"))
        adapterPaymentMethods.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.paymentMethods.adapter = adapterPaymentMethods
    }

    private fun setUpRandomData() {
        // need to use dot as decimal separator
        val amountFormatted = AmountWithCurrency.formatDecimal(BigDecimal((((1..99999).random()) / 100.00).toString()))
        binding.amount.setText(amountFormatted)
        binding.currency.setText("EUR")
        binding.reference.setText("Reference " + ((1..999999).random()).toString())
    }

    private fun toggleUiComponents() {
        runOnUiThread {
            binding.progressBar.visibility = if (binding.progressBar.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            binding.button.isEnabled = !binding.button.isEnabled
        }
    }

    private fun validateAndSetUpPayment() {
        toggleUiComponents()

        val paymentMethod = binding.paymentMethods.selectedItem
        val amountText = binding.amount.text.toString()
        val amount = if (amountText.isBlank()) BigDecimal(0) else amountText.toBigDecimal()
        if (amount < BigDecimal(0.01)) {
            Toast.makeText(this, "Set the amount to 0.01 at least", Toast.LENGTH_SHORT).show()
            return
        }

        val currency = binding.currency.text.toString()
        if (currency.isBlank()) {
            Toast.makeText(this, "Fill the currency", Toast.LENGTH_SHORT).show()
            return
        }

        val reference = binding.reference.text.toString()

        lifecycleScope.launch {
            when (paymentMethod) {
                "Wire" -> setupWirePayment(amount, currency, reference)
                "Card" -> setupCardPayment(amount, currency, reference)
            }

            toggleUiComponents()
        }
    }

    private suspend fun setupWirePayment(amount: BigDecimal, currency: String, reference: String) {
        val wireRequest = WireRequest(
            MerchantIdentification(DemoPaymentData.ProjectId), PaymentInformation(
                AmountWithCurrency(amount, currency),
                References(reference),
                isRedirect = true,
                localization = "SK",
                country = "SK"
            ), CallbackUrls(
                "${DemoPaymentData.BaseRedirectUrl}?q=success",
                "${DemoPaymentData.BaseRedirectUrl}?q=cancel",
                "${DemoPaymentData.BaseRedirectUrl}?q=error"
            )
        )

        val result = wireRequest.createPaymentRequest(tokenProvider)
        if (result.isFailure) {
            Toast.makeText(this@MainActivity,result.exceptionOrNull()?.message ?: "Something went wrong", Toast.LENGTH_LONG)
                .show()
            return
        }

        val viaWebView = binding.webViewRb.isChecked
        if (viaWebView) {
            result.getOrThrow().launchPopupWebView(
                this@MainActivity,
                DemoPaymentData.BaseRedirectUrl,
                RedirectActivity::class.java.name,
                applicationContext.packageName
            )
        } else {
            result.getOrThrow().launchChromeCustomTabs(
                this@MainActivity,
                PaymentResponse.PopUpOptions(
                    ContextCompat.getColor(this, android.R.color.holo_blue_light),
                    ContextCompat.getColor(this, android.R.color.holo_blue_dark)
                )
            )
        }
    }

    private suspend fun setupCardPayment(amount: BigDecimal, currency: String, reference: String) {
        val request = CardRequest(
            MerchantIdentification(DemoPaymentData.ProjectId),
            PaymentInformation(
                AmountWithCurrency(amount, currency),
                References(reference),
                isRedirect = true,
                localization = "SK",
                country = "SK"
            ),
            CardTransaction(CardPaymentType.Purchase),
            CallbackUrls(
                "${DemoPaymentData.BaseRedirectUrl}?q=success",
                "${DemoPaymentData.BaseRedirectUrl}?q=cancel",
                "${DemoPaymentData.BaseRedirectUrl}?q=error"
            )
        )

        val result = request.createPaymentRequest(tokenProvider)
        if (result.isFailure) {
            Toast.makeText(this@MainActivity,result.exceptionOrNull()?.message ?: "Something went wrong", Toast.LENGTH_LONG)
                .show()
            return
        }

        val viaWebView = binding.webViewRb.isChecked
        if (viaWebView) {
            result.getOrThrow().launchPopupWebView(
                this@MainActivity,
                DemoPaymentData.BaseRedirectUrl,
                RedirectActivity::class.java.name,
                applicationContext.packageName
            )
        } else {
            result.getOrThrow().launchChromeCustomTabs(
                this@MainActivity,
                PaymentResponse.PopUpOptions(
                    ContextCompat.getColor(this, android.R.color.holo_blue_light),
                    ContextCompat.getColor(this, android.R.color.holo_blue_dark)
                )
            )
        }
    }

}

