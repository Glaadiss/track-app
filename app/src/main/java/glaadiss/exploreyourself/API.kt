package glaadiss.exploreyourself

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import org.json.JSONObject


fun authInterceptor() = { next: (Request) -> Request ->
    { req: Request ->
        val account = API.getAccountSuspended()
        req.header(Headers.AUTHORIZATION, account.idToken!!)
        next(req)
    }
}

fun jsonInterceptor() = { next: (Request) -> Request ->
    { req: Request ->
        req.header("Content-Type" to "application/json")
        next(req)
    }
}


fun getPath(resource: String): String {
    return ContextProvider.context.getString(R.string.base_url) + resource
}

fun logResponse(result: Result<String, FuelError>) =
    when (result) {
        is Result.Failure -> {
            val ex = result.getException()
            Log.i("Request-failure", ex.toString())
            Logger.write("Request-failure $ex")
        }
        is Result.Success -> {
            val data = result.get()
            Log.i("Request-success", data)
            Logger.write("Request-success $data")

        }
        else -> Logger.write("Request-failure unexpected failure")
    }


object API {
    const val RC_SIGN_IN = 101

    private val googleClient = createGoogleClient()

    init {
        FuelManager.instance.addRequestInterceptor(authInterceptor())
        FuelManager.instance.addRequestInterceptor(jsonInterceptor())
    }

    private fun getGso(): GoogleSignInOptions {
        val clientId = ContextProvider.context.getString(R.string.test_client_id)
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
    }

    private fun createGoogleClient(): GoogleSignInClient {
        return GoogleSignIn.getClient(ContextProvider.context, getGso())
    }

    fun authenticate(activity: Activity) {
        googleClient.silentSignIn().addOnFailureListener {
            Toast.makeText(activity.applicationContext, it.toString(), Toast.LENGTH_LONG).show()
            activity.startActivityForResult(googleClient.signInIntent, RC_SIGN_IN)
        }
    }

    fun getAccountSuspended(): GoogleSignInAccount {
        val apiClient = GoogleApiClient
            .Builder(ContextProvider.context)
            .addApi(Auth.GOOGLE_SIGN_IN_API, getGso())
            .build()
        apiClient.blockingConnect()
        val result = Auth.GoogleSignInApi.silentSignIn(apiClient).await()

        apiClient.disconnect()

        return result.signInAccount!!
    }

    private fun getBody(map: Map<Any, Any>) =
        """${JSONObject(map)}"""

    fun rate(rate: Number) {
        val body = getBody(mapOf("rating" to rate))
        Fuel.post(getPath("rate")).body(body).responseString { _, _, res ->
            logResponse(res)
        }
    }

    fun sendStats(statsJson: String) {
        Fuel.post(getPath("activities")).body(statsJson).responseString { _, _, res -> logResponse(res) }
    }

    fun getReport() {
        Fuel.get(getPath("report")).responseString { _, _, res -> logResponse(res) }
    }

}


