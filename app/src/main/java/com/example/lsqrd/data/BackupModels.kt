package com.example.lsqrd.data

import org.json.JSONArray
import org.json.JSONObject

data class BackupField(
    val label : String,
    val value: String,
    val isSecret: Boolean
)

data class BackupCredential(
    val name: String,
    val fields: List<BackupField>
)

data class BackupVault(
    val name: String,
    val credentials: List<BackupCredential>
)

data class BackupPayload(
    val version: Int = 1,
    val vaults: List<BackupVault>
){
    fun toJson(): String {
        val vaultsArray= JSONArray()
        for (vault in vaults){
            val credsArray = JSONArray()
            for(cred in vault.credentials){
                val fieldsArray = JSONArray()
                for (field in cred.fields){
                    fieldsArray.put(JSONObject().apply {
                        put("label", field.label)
                        put("value", field.value)
                        put("isSecret", field.isSecret)
                    })
                }
                credsArray.put(JSONObject().apply {
                    put("name", cred.name)
                    put("fields", fieldsArray)
                })
            }
            vaultsArray.put(JSONObject().apply {
                put("name", vault.name)
                put("credentials", credsArray)
            })
        }
        return JSONObject().apply {
            put("version", version)
            put("vaults", vaultsArray)
        }.toString()
    }

    companion object{
        fun fromJson(json: JSONObject): BackupPayload{
            val version = json.getInt("version")
            val vaultsArray = json.getJSONArray("vaults")
            val vaults = (0 until  vaultsArray.length()).map { i ->
                val vaultJson =vaultsArray.getJSONObject(i)
                val credsArray= vaultJson.getJSONArray("credentials")
                val credentials = (0 until credsArray.length()).map { j ->
                    val credJson = credsArray.getJSONObject(j)
                    val fieldsArray = credJson.getJSONArray("fields")
                    val fields = (0 until fieldsArray.length()).map { k ->
                        val fieldJson = fieldsArray.getJSONObject(k)
                        BackupField(
                            label = fieldJson.getString("label"),
                            value = fieldJson.getString("value"),
                            isSecret = fieldJson.getBoolean("isSecret")
                        )
                    }
                    BackupCredential(name = credJson.getString("name"), fields = fields)
                }
                BackupVault(name = vaultJson.getString("name"), credentials = credentials)
            }
            return BackupPayload(version = version, vaults = vaults)
        }
    }
}