package wfy.photolabeller.server.web.controller;

import wfy.photolabeller.server.BasicRoundController;
import wfy.photolabeller.server.FederatedServerImpl;
import wfy.photolabeller.server.core.FederatedAveragingStrategy;
import wfy.photolabeller.server.core.domain.repository.ServerRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wfy.photolabeller.server.core.datasource.*;
import wfy.photolabeller.server.core.domain.model.*;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Fangyuan Wang
 */

@RestController
@RequestMapping("/service/federatedservice")
public class FederatedController {

    private static FederatedServer federatedServer;


    @GetMapping(path = "/available")
    public String available() {
        return "yes";
    }

    @PostMapping(path = "/model")
    public Boolean pushGradient(@RequestParam("file") MultipartFile is, @RequestParam("samples") int samples) throws IOException {
        if (is == null) {
            return false;
        } else {
            //byte[] bytes = IOUtils.toByteArray(is);

            federatedServer.pushUpdate(is.getBytes(), samples);
            return true;
        }
    }


    @GetMapping(path = "/model")
    public Response getFile() {
        File file = federatedServer.getModelFile();
        String fileName = federatedServer.getUpdatingRound().getModelVersion() + ".zip";
        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        return response.build();
    }


    @GetMapping(path = "/currentRound", produces = {MediaType.APPLICATION_JSON_VALUE})
    public String getCurrentRound() {
        return federatedServer.getUpdatingRoundAsJson();
    }

    @PostConstruct
    public void init() throws IOException {
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
            UpdatesStrategy updatesStrategy = new FederatedAveragingStrategy(repository, Integer.parseInt(properties.getProperty("layer_index")));

            UpdatingRound currentUpdatingRound = repository.retrieveCurrentUpdatingRound();

            long timeWindow = Long.parseLong(properties.getProperty("time_window"));
            int minUpdates = Integer.parseInt(properties.getProperty("min_updates"));

            RoundController roundController = new BasicRoundController(repository, currentUpdatingRound, timeWindow, minUpdates);

            federatedServer = FederatedServerImpl.Companion.getInstance();
            federatedServer.initialise(repository, updatesStrategy, roundController, logger, properties);

            // 当服务器启动时，开始新的一轮
            roundController.startRound();
        }
    }

}

