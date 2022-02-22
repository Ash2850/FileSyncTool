package fileSynchronizationPackage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class FileSyncProvider implements CommandLineRunner
{
    public static void main(String[] args) throws IOException
    {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(FileSyncProvider.class, args);
        fileSyncBean(applicationContext);
    }

    private static void fileSyncBean(ConfigurableApplicationContext applicationContext) throws IOException {

        File source = new File("src\\main\\resources\\TestFileSync\\A-Source");
        File destination = new File("src\\main\\resources\\TestFileSync\\B-Destination");

        FileSyncUtility fileSyncUtilityBean = applicationContext.getBean(FileSyncUtility.class);
        FileSyncService fileSyncServiceBean = applicationContext.getBean(FileSyncService.class);

        if (!destination.exists()) {
            destination.mkdir();
            fileSyncUtilityBean.copyDirectory(source, destination);
            destination.setLastModified(source.lastModified());
            return;
        }
        fileSyncServiceBean.synchronizeTwoFiles(source, destination);
        destination.setLastModified(source.lastModified());
    }
    Logger LOGGER = LoggerFactory.getLogger(FileSyncProvider.class);
    @Override
    public void run(String... args) throws Exception
    {
        LOGGER.info("run: {}", args);
    }
}
