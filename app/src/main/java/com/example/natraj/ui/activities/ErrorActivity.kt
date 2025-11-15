package com.example.natraj.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.natraj.MainActivity
import com.example.natraj.R
import com.example.natraj.util.error.AppErrorHandler
import com.example.natraj.util.error.ErrorType

class ErrorActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        
        val errorTitle = findViewById<TextView>(R.id.error_title)
        val errorMessage = findViewById<TextView>(R.id.error_message)
        val retryButton = findViewById<Button>(R.id.retry_button)
        val goHomeButton = findViewById<Button>(R.id.go_home_button)
        
        // Get error details from intent
        val title = intent.getStringExtra("error_title") ?: "Oops! Something went wrong"
        val message = intent.getStringExtra("error_message") ?: "We encountered an error while processing your request."
        val actionText = intent.getStringExtra("action_text") ?: "Retry"
        val showRetry = intent.getBooleanExtra("show_retry", true)
        
        errorTitle.text = title
        errorMessage.text = message
        retryButton.text = actionText
        
        if (!showRetry) {
            retryButton.visibility = android.view.View.GONE
        }
        
        retryButton.setOnClickListener {
            finish()
        }
        
        goHomeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
    
    companion object {
        private const val TAG = "ErrorActivity"
        
        fun show(
            context: Context,
            title: String,
            message: String,
            actionText: String = "Retry",
            showRetry: Boolean = true
        ) {
            val intent = Intent(context, ErrorActivity::class.java)
            intent.putExtra("error_title", title)
            intent.putExtra("error_message", message)
            intent.putExtra("action_text", actionText)
            intent.putExtra("show_retry", showRetry)
            context.startActivity(intent)
        }
        
        fun showFromErrorType(
            context: Context,
            errorType: ErrorType,
            exception: Exception? = null,
            showRetry: Boolean = true
        ) {
            // Log technical details for debugging
            exception?.let {
                Log.e(TAG, "Error occurred: ${errorType.name}", it)
            }
            
            // Get user-friendly error message
            val errorMessage = AppErrorHandler.getErrorMessage(errorType)
            
            val intent = Intent(context, ErrorActivity::class.java)
            intent.putExtra("error_title", errorMessage.title)
            intent.putExtra("error_message", errorMessage.message)
            intent.putExtra("action_text", errorMessage.actionText)
            intent.putExtra("show_retry", showRetry)
            context.startActivity(intent)
        }
        
        fun showFromException(
            context: Context,
            exception: Exception,
            showRetry: Boolean = true
        ) {
            // Determine error type from exception
            val errorType = AppErrorHandler.fromException(exception)
            showFromErrorType(context, errorType, exception, showRetry)
        }
    }
}
