package wfy.photolabeller.server.web;

import wfy.photolabeller.server.BasicRoundController;
import wfy.photolabeller.server.FederatedServerImpl;
import wfy.photolabeller.server.core.FederatedAveragingStrategy;
import wfy.photolabeller.server.core.domain.repository.ServerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import wfy.photolabeller.server.core.datasource.*;
import wfy.photolabeller.server.core.domain.model.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootTest
class ServerApplicationTests {

    private static FederatedServer federatedServer;


    @Test
    void contextLoads() throws IOException {

        if (federatedServer == null) {
            // TODO Inject!
            // TODO Properties to SharedConfig
            Properties properties = new Properties();

            ClassPathResource resource = new ClassPathResource("local.properties");

            properties.load(resource.getInputStream());

            java.nio.file.Path rootPath = Paths.get(properties.getProperty("model_dir"));
            FileDataSource fileDataSource = new FileDataSourceImpl(rootPath);
            MemoryDataSource memoryDataSource = new MemoryDataSourceImpl();
            ServerRepository repository = new ServerRepositoryImpl(fileDataSource, memoryDataSource);
            Logger logger = System.out::println;
            UpdatesStrategy updatesStrategy = new FederatedAveragingStrategy(repository, Integer.valueOf(properties.getProperty("layer_index")));

            UpdatingRound currentUpdatingRound = repository.retrieveCurrentUpdatingRound();

            long timeWindow = Long.valueOf(properties.getProperty("time_window"));
            int minUpdates = Integer.valueOf(properties.getProperty("min_updates"));

            RoundController roundController = new BasicRoundController(repository, currentUpdatingRound, timeWindow, minUpdates);

            federatedServer = FederatedServerImpl.Companion.getInstance();
            federatedServer.initialise(repository, updatesStrategy, roundController, logger, properties);

            // We're starting a new round when the server starts
            roundController.startRound();
        }
    }

}
