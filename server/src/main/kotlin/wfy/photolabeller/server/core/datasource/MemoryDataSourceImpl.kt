package wfy.photolabeller.server.core.datasource

import wfy.photolabeller.server.core.domain.model.ClientUpdate

class MemoryDataSourceImpl: MemoryDataSource {
    private var clientUpdates = mutableListOf<ClientUpdate>()

    override fun addUpdate(clientUpdate: ClientUpdate) {
        clientUpdates.add(clientUpdate)
    }

    override fun getUpdates(): List<ClientUpdate> = clientUpdates

    override fun clear(): Unit = clientUpdates.clear()

}