package sk.trustpay.api.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import sk.trustpay.api.demo.databinding.ActivityRedirectBinding

class RedirectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedirectBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedirectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val redirectUri = intent?.data
        if (redirectUri != null) {
            Log.d("PaidActivity", redirectUri.toString())

            binding.reference.text = redirectUri.getQueryParameter("Reference")
            binding.resultCode.text = redirectUri.getQueryParameter("ResultCode")
            binding.paymentRequestId.text = redirectUri.getQueryParameter("PaymentRequestId")
        }
    }
}