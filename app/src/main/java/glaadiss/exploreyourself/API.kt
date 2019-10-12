package glaadiss.exploreyourself

import android.app.Activity
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
    init {
        FuelManager.instance.addRequestInterceptor(authInterceptor())
        FuelManager.instance.addRequestInterceptor(jsonInterceptor())
    }

    private fun createGoogleClient(): GoogleSignInClient {
        val clientId = ContextProvider.context.getString(R.string.server_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(ContextProvider.context, gso)
    }

    fun authenticate(activity: Activity) {
        val googleClient = createGoogleClient()
        googleClient.silentSignIn().addOnFailureListener {
            activity.startActivity(googleClient.signInIntent)
        }
    }

    fun getAccountSuspended(): GoogleSignInAccount {
        val googleClient = createGoogleClient()
        val signingProcess = googleClient.silentSignIn()
        while (!signingProcess.isComplete) {
            Thread.sleep(10)
        }
        return signingProcess.result!!
    }

    private fun getBody(map: Map<Any, Any>) =
        """${JSONObject(map)}"""

    fun rate(rate: Number) {
        val body = getBody(mapOf("rating" to rate))
        Fuel.post(getPath("rate")).body(body).responseString { _, _, res -> logResponse(res)
        }
    }

    fun sendStats(statsJson: String) {
        Fuel.post(getPath("activities")).body(statsJson).responseString { _, _, res -> logResponse(res) }
    }

}


