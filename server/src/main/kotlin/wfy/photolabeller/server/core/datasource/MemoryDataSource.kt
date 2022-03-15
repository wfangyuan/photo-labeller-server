package wfy.photolabeller.server.core.datasource

import wfy.photolabeller.server.core.domain.model.ClientUpdate

interface MemoryDataSource {
    fun addUpdate(clientUpdate: ClientUpdate)
    fun getUpdates(): List<ClientUpdate>
    fun clear()
}