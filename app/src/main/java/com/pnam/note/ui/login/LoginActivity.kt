package com.pnam.note.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.IBinder
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pnam.note.database.data.models.EmailPassword
import com.pnam.note.databinding.ActivityLoginBinding
import com.pnam.note.ui.adapters.login.LoginAdapter
import com.pnam.note.ui.adapters.login.LoginItemClickListener
import com.pnam.note.ui.dashboard.DashboardActivity
import com.pnam.note.ui.forgotpassword.ForgotPasswordActivity
import com.pnam.note.ui.register.RegisterActivity
import com.pnam.note.utils.AppConstants.APP_NAME
import com.pnam.note.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var loginAdapter: LoginAdapter

    private val loginClick: View.OnClickListener by lazy {
        View.OnClickListener {
            val email = binding.edtEmail.text?.trim().toString()
            val password = binding.edtPassword.text?.trim().toString()
            if (email.isEmpty() || password.isEmpty()) {
                binding.tilPassword?.let { til ->
                    til.isErrorEnabled = true
                    til.errorContentDescription = "All input are required."
                }
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail?.let { til ->
                    til.errorContentDescription = "Your email is invalid"
                }
            } else {
                binding.btnLogin?.windowToken?.let { btn -> hideKeyboard(btn) }
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.login(email, password)
                }
            }
        }
    }

    private val emailClick: View.OnClickListener by lazy {
        View.OnClickListener {
            if (binding.rcvLogins?.visibility != View.VISIBLE) {
                binding.rcvLogins?.visibility = View.VISIBLE
            }
        }
    }

    private val registerClick: View.OnClickListener by lazy {
        View.OnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private val forgotClick: View.OnClickListener by lazy {
        View.OnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rcvLogins?.layoutManager = LinearLayoutManager(this)
        binding.let {
            it.btnLogin?.setOnClickListener(loginClick)
            it.btnRegister?.setOnClickListener(registerClick)
            it.btnForgot?.setOnClickListener(forgotClick)
            it.edtEmail.setOnClickListener(emailClick)
            it.edtEmail.addTextChangedListener {
                binding.rcvLogins?.visibility = View.GONE
            }
        }
        initLoginObserver()
        initSavedLoginObserver()
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.getSavedLogin()
        }
    }

    private fun initLoginObserver() {
        viewModel.login.observe(this) {
            when (it) {
                is Resource.Loading -> {
                    binding.load?.visibility = View.VISIBLE

                }
                is Resource.Success -> {
                    binding.load?.visibility = View.INVISIBLE
                    val data: Intent = Intent(this, DashboardActivity::class.java).apply {
                        applicationContext.getSharedPreferences(
                            APP_NAME,
                            Context.MODE_PRIVATE
                        ).edit().putString(EMAIL, it.data.email).apply()

                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(data)
                }
                is Resource.Error -> {
                    binding.load?.visibility = View.INVISIBLE
                    binding.tilPassword?.let { til ->
                        til.isErrorEnabled = true
                        til.errorContentDescription = it.message
                    }
                }
            }
        }
        viewModel.error.observe(this) {
            binding.load?.visibility = View.INVISIBLE
            binding.tilPassword?.let { til ->
                til.isErrorEnabled = true
                til.errorContentDescription = viewModel.error.value
            }
        }
    }

    private fun initSavedLoginObserver() {
        viewModel.savedLogin.observe(this) {
            when (it) {
                is Resource.Loading -> {

                }
                is Resource.Success -> {
                    if (it.data.size != 0) {
                        loginAdapter = LoginAdapter(
                            it.data,
                            object : LoginItemClickListener {
                                override fun onClick(emailPassword: EmailPassword) {
                                    binding.edtEmail.setText(emailPassword.email)
                                    binding.edtPassword.setText(emailPassword.password)
                                    binding.rcvLogins?.visibility = View.GONE
                                }

                                override fun onDeleteClick(email: String, position: Int) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        viewModel.loginDao.deleteLogin(email)
                                    }
                                    loginAdapter.removeAt(position)
                                    if (loginAdapter.itemCount == 0) {
                                        binding.rcvLogins?.visibility = View.GONE
                                    }
                                }
                            }
                        )
                        binding.rcvLogins?.adapter = loginAdapter
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val rect = Rect()
        binding.rcvLogins?.getGlobalVisibleRect(rect)
        if (!rect.contains(ev!!.rawX.toInt(), ev.rawY.toInt())) {
            binding.rcvLogins?.visibility = View.GONE
        }
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val EMAIL: String = "email"
    }

    private fun hideKeyboard(element: IBinder) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(element, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
    }
}