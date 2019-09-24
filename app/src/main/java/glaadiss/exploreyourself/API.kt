package glaadiss.exploreyourself

import android.app.Activity
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.Request
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

object API {
    init {
        FuelManager.instance.addRequestInterceptor(authInterceptor())
        FuelManager.instance.addRequestInterceptor(jsonInterceptor())
    }


    private val googleClient = createGoogleClient()

    private fun createGoogleClient(): GoogleSignInClient {
        val clientId = ContextProvider.context.getString(R.string.server_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(ContextProvider.context, gso)
    }

    fun authenticate(activity: Activity) {
        googleClient.silentSignIn().addOnFailureListener {
            activity.startActivity(googleClient.signInIntent)
        }
    }

    fun getAccountSuspended(): GoogleSignInAccount {
        val signingProcess = googleClient.silentSignIn()
        while (!signingProcess.isComplete) {
            Thread.sleep(10)
        }
        return signingProcess.result!!
    }

    private fun getBody(map: Map<Any, Any>) =
        """${JSONObject(map)}"""

    fun rate(rate: Number) {
        val body = getBody(mapOf("rate" to rate))
        Fuel.post(getPath("rate")).body(body).response { _, _, result ->
            Log.i("Request", result.toString())
        }

    }

}


