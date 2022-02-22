package fileSynchronizationPackage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.*;

import static org.hibernate.internal.CoreLogging.logger;

@Component
public class FileSyncService {
    @Autowired
    FileSyncUtility fileSyncUtility;

    private static ExecutorService fileSyncExecutor;
    Logger logger = LoggerFactory.getLogger(FileSyncService.class);

    public void synchronizeTwoFiles(File source, File destination) throws IOException {
        logger.info("Synchronizing 2 directories : " + source.getName() + "and : " + destination.getName());
        try
        {
            fileSyncExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            Future<Void> future = fileSyncExecutor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    synchronizeByDeletion(source, destination); //Based on sourceSet - Independent
                    synchronizeByCreation(source, destination); //Based on destinationSet - Not Independent - Make the method synchronized
                    synchronizeByWalk(source, destination); //Based on sourceSet - Independent
                    return null;
                }
            });
            try
            {
                future.get(10, TimeUnit.SECONDS);
            }
            catch (TimeoutException ex)
            {
                future.cancel(true);
                throw ex;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            fileSyncExecutor.shutdown();
        }
    }

    private void synchronizeByDeletion(File source, File destination) throws IOException
    {
        logger.info("Synchronizing directories by Deletion in : " + destination.getName());
        HashSet<String> sourceSet = new HashSet<>(Arrays.asList(source.list()));
        for(File target : destination.listFiles())
        {
            if (!sourceSet.contains(target.getName()))
            {
                fileSyncUtility.deleteDirectory(target);
            }
        }
    }

    private void synchronizeByWalk(File source, File destination) throws IOException
    {
        logger.info("Synchronizing directories by Walking in : " + source.getName() + "and : " + destination.getName());
        HashSet<String> sourceSet = new HashSet<>(Arrays.asList(source.list()));
        String sourceRootPath = source.getAbsolutePath();
        for(File target : destination.listFiles())
        {
            if (sourceSet.contains(target.getName()))
            {
                String sourceFilePath = sourceRootPath.concat("\\").concat(target.getName());
                File sourceFile = new File(sourceFilePath);
                if((sourceFile.isDirectory() && ! target.isDirectory()) || (!sourceFile.isDirectory() && target.isDirectory()))
                {
                    fileSyncUtility.deleteDirectory(target);
                    fileSyncUtility.copyDirectory(sourceFile, target);

                }
                else if(sourceFile.isDirectory() && target.isDirectory() && !(fileSyncUtility.isFilesIdentical(sourceFile, target))
                        && sourceFile.listFiles().length > 0 && target.listFiles().length > 0)
                {
                    synchronizeTwoFiles(sourceFile, target);
                }
                else if(sourceFile.isFile() && target.isFile() && !(fileSyncUtility.isFilesIdentical(sourceFile, target)))
                {
                    fileSyncUtility.copyDirectory(sourceFile, target);
                }
                target.setLastModified(sourceFile.lastModified());
            }
        }
    }

    private synchronized void synchronizeByCreation(File source, File destination) throws IOException
    {
        logger.info("Synchronizing directories by Creation of files from : " + source.getName() + "in : " + destination.getName());
        HashSet<String> destinationSet = new HashSet<>(Arrays.asList(destination.list()));
        String destinationRootPath  = destination.getAbsolutePath();
        for(File sourceFile : source.listFiles())
        {
            if (!destinationSet.contains(sourceFile.getName()))
            {
                String destinationFilePath = destinationRootPath.concat("\\").concat(sourceFile.getName());
                fileSyncUtility.copyDirectory(sourceFile, new File(destinationFilePath));
            }
        }
    }

}

