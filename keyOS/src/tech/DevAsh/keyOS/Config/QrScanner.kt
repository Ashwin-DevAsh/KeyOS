package tech.DevAsh.keyOS.Config

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.launcher3.BuildConfig.QR_KEY
import com.android.launcher3.R
import com.budiyev.android.codescanner.*
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import retrofit2.Call
import retrofit2.Response
import tech.DevAsh.KeyOS.Database.RealmHelper
import tech.DevAsh.KeyOS.Helpers.AlertHelper
import tech.DevAsh.keyOS.Api.IQRCodeService
import tech.DevAsh.keyOS.Database.User
import tech.DevAsh.keyOS.KioskApp
import javax.inject.Inject


class QrScanner : AppCompatActivity() {

    @Inject
    lateinit var qrCodeService: IQRCodeService

    var mProgressDialog:ProgressDialog?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)
        mProgressDialog = ProgressDialog(this, R.style.MyProgressDialog)
        KioskApp.applicationComponents?.inject(this)
        loadView()
        loadScanner()

    }

    fun loadView(){
        val w: Window = window
        w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                  )
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private fun loadScanner(){
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        val codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                try {
                    verifyQr(it.text)
                }catch (e: Throwable){
                    e.printStackTrace()
                }

            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, getString(R.string.camera_not_init) + " ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
        codeScanner.startPreview()
    }


    private fun verifyQr(data: String){
        println(data)
        mProgressDialog?.setMessage(getString(R.string.please_wait))
        mProgressDialog?.setCanceledOnTouchOutside(false)
        mProgressDialog?.show()
        try {
            val id = Jwts.parser().setSigningKey(QR_KEY).parseClaimsJws(data).body["id"]
            getPolicyData(id.toString())
        } catch (exception: ExpiredJwtException) {
            exception.printStackTrace()
            onFailure(getString(R.string.invalid_qr))
        }catch (e: UnsupportedJwtException){
            onFailure(getString(R.string.invalid_qr))

        }catch (e: MalformedJwtException){
            onFailure(getString(R.string.invalid_qr))

        }catch (e: io.jsonwebtoken.SignatureException){
            onFailure(getString(R.string.invalid_qr))


        }catch (e: java.lang.IllegalArgumentException){
            onFailure(getString(R.string.invalid_qr))

        }catch (e:Throwable){
            onFailure(getString(R.string.invalid_qr))

        }
    }

    private fun getPolicyData(id: String){
        qrCodeService.getPolicyData(id)?.enqueue(callback)
    }

    var callback = object: retrofit2.Callback<User?>{
        override fun onResponse(call: Call<User?>, response: Response<User?>) {
            if(response.body()!=null){
                Handler().postDelayed({ onSuccess(response.body()!!) },800)
                return
            }


            println(response)
            onFailure(getString(R.string.failed))

        }

        override fun onFailure(call: Call<User?>, t: Throwable) {
            t.printStackTrace()
            onFailure(getString(R.string.failed))
        }

    }

    fun onSuccess(user: User){
        mProgressDialog?.dismiss()
        RealmHelper.updateUser(user)
        Handler().postDelayed({onBackPressed()}, 500)
        Handler().postDelayed({ AlertHelper.showToast(getString(R.string.successfully_done), this)
                              }, 1000)
    }

    fun onFailure(text:String){
        mProgressDialog?.dismiss()
        Handler().postDelayed({
                                  onBackPressed()

                              }, 500)
        Handler().postDelayed({
                                  AlertHelper.showToast(text, this)
                              }, 1000)
    }

}