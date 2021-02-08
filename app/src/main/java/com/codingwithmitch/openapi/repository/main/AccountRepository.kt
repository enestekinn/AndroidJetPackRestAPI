package com.codingwithmitch.openapi.repository.main

import android.accounts.Account
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.codingwithmitch.openapi.api.GenericResponse
import com.codingwithmitch.openapi.api.main.OpenApiMainService
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken
import com.codingwithmitch.openapi.persistence.AccountPropertiesDao
import com.codingwithmitch.openapi.repository.JobManager
import com.codingwithmitch.openapi.repository.NetworkBoundResource
import com.codingwithmitch.openapi.session.SessionManager
import com.codingwithmitch.openapi.ui.Data
import com.codingwithmitch.openapi.ui.DataState
import com.codingwithmitch.openapi.ui.Response
import com.codingwithmitch.openapi.ui.ResponseType
import com.codingwithmitch.openapi.ui.main.account.state.AccountViewState
import com.codingwithmitch.openapi.util.AbsentLiveData
import com.codingwithmitch.openapi.util.ApiSuccessResponse
import com.codingwithmitch.openapi.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
): JobManager("AccountRepository") {
    private val TAG ="AppDebug"



    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties,AccountProperties, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {
            override fun loadFromCache(): LiveData<AccountViewState> {
                return  accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                        object : LiveData<AccountViewState>(){
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {

                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }

            }


            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    //finish by viewing the db cache
                    result.addSource(loadFromCache()){viewState ->
                        onCompleteJob(DataState.data(
                            data = viewState,
                            response = null
                        ))

                    }
                }

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)
                withContext(Main){
                    createCacheRequestAndReturn()

                }
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
               return openApiMainService
                   .getAccountProperties(
                       "Token ${authToken.token}"

                   )

            }

            override fun setJob(job: Job) {

                addJob("getAccountProperties",job)
            }



        }.asLiveData()
    }

    fun saveAccountProperties(authToken: AuthToken, accountProperties: AccountProperties) : LiveData<DataState<AccountViewState>>{
        return object: NetworkBoundResource<GenericResponse,Any,AccountViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false

        ){

            //Not applicable
            override suspend fun createCacheRequestAndReturn() {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {



            updateLocalDb(null)


                withContext(Main) {

                    //finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response,ResponseType.Toast())
                        )
                    )
                }

            }
            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {

return openApiMainService.saveAccountProperties(
    "Token ${authToken.token!!}",
    accountProperties.email,
    accountProperties.username
)
            }



            //not used in this case
            override fun loadFromCache(): LiveData<AccountViewState> {

                return  AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username

                )
            }

            override fun setJob(job: Job) {

               addJob("saveAccountProperties",job)

            }

        }.asLiveData()
    }


    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
    newPassword: String,
    confirmPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any,AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
        false
        ){
            //not applicable
            override suspend fun createCacheRequestAndReturn() {

            }


            //not applicable in this case

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                withContext(Main){

                    //finish with success reponse
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response,ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {

                return openApiMainService.updatePassword(
                    "Token ${authToken.token!!}",
                    currentPassword,
                    newPassword,
                    confirmPassword
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {

                return  AbsentLiveData.create()
            }


            //not applicable in this case

            override suspend fun updateLocalDb(cacheObject: Any?) {
            }

            override fun setJob(job: Job) {

              addJob("updatePassword",job)
            }

        }.asLiveData()
    }


}