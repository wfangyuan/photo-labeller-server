package wfy.photolabeller.server

import wfy.photolabeller.server.core.FederatedAveragingStrategy
import wfy.photolabeller.server.core.datasource.FileDataSourceImpl
import wfy.photolabeller.server.core.datasource.MemoryDataSourceImpl
import wfy.photolabeller.server.core.datasource.ServerRepositoryImpl
import org.springframework.core.io.ClassPathResource
import java.nio.file.Paths
import java.util.*

fun main(args: Array<String>) {
    val properties = Properties()

    val resource = ClassPathResource("local.properties")

    properties.load(resource.inputStream)
   // properties.load(FileInputStream("./server/local.properties"))

    val rootPath = Paths.get(properties.getProperty("model_dir"))
    val fileDataSource = FileDataSourceImpl(rootPath)
    val memoryDataSource = MemoryDataSourceImpl()
    val repository = ServerRepositoryImpl(fileDataSource, memoryDataSource)
    repository.restoreClientUpdates()
    val updatesStrategy = FederatedAveragingStrategy(repository, 3)

    updatesStrategy.processUpdates()
}