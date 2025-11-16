import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Essentials.IDirectoryService
import com.base.abstractions.Essentials.IPreferences
import com.base.abstractions.Platform.IZipService
import com.base.mvvm.Navigation.IPageNavigationService
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class KoinResolver : KoinComponent
{
    @Throws(Throwable::class)
    fun GetLoggingService() : ILoggingService = SafeCall { get() }

    @Throws(Throwable::class)
    fun GetPreferences(): IPreferences = SafeCall { get() }

    @Throws(Throwable::class)
    fun GetDirectoryService(): IDirectoryService = SafeCall { get() }

    @Throws(Throwable::class)
    fun GetNavigationService(): IPageNavigationService = SafeCall { get() }

    @Throws(Throwable::class)
    fun GetZipService(): IZipService = SafeCall { get() }


    //if it crash then it will print the whole stacktrace, error details in ouput console
    //so it will be easier to investigate the issue
    private fun <T> SafeCall(getServiceMethod: () -> T) : T
    {
        try
        {
            val service = getServiceMethod()
            return service
        }
        catch (ex: Throwable)
        {
            println(ex.stackTraceToString())
            throw ex
        }
    }

}
