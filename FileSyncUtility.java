package fileSynchronizationPackage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.hibernate.internal.CoreLogging.logger;

@Component
public class FileSyncUtility {
    Logger logger = LoggerFactory.getLogger(FileSyncUtility.class);

    public boolean isFilesIdentical(File source, File destination) throws IOException
    {
        logger.info("Checking for identical files in source: " + source.getName() + " & destination: " + destination.getName());
        Path sourcePath = Paths.get(source.getPath());
        Path destinationPath = Paths.get(destination.getPath());
        long sourceTimeStamp = source.lastModified();
        long destinationTimeStamp = destination.lastModified();
        long sourceSize = source.length();
        long destinationSize = destination.length();

        if(sourceTimeStamp == destinationTimeStamp && sourceSize == destinationSize)
        {
            if(source.isDirectory() && destination.isDirectory()) return true;
            else
            {
                if(Files.mismatch(sourcePath, destinationPath) == -1){
                    return true;
                }
            }
        }
        return false;
    }



    public void copyDirectory(File source, File destination) throws IOException
    {
        logger.info("Copying files from source: " + source.getName() + " to destination: " + destination.getName());
        if(source.isFile() && destination.isFile())
        {
            Files.copy(Paths.get(source.getPath()), Paths.get(destination.getPath()), StandardCopyOption.REPLACE_EXISTING);
        }
        else if(source.listFiles().length == 0)//Empty directory
        {
            Files.copy(Paths.get(source.getPath()), Paths.get(destination.getPath()), StandardCopyOption.REPLACE_EXISTING);
        }
        else
        {
            if(source.isDirectory() && !destination.exists())
            {
                destination.mkdir();
            }
            for(File subfile: source.listFiles())
            {
                String newFilePath = destination.getPath().concat("\\").concat(subfile.getName());
                if(subfile.isDirectory())
                {
                    File temp = new File(newFilePath);
                    temp.mkdir();
                    copyDirectory(subfile, temp);

                }
                else if(subfile.isFile())
                {
                    Path sourcePath = Paths.get(subfile.getPath());
                    Path targetPath = Paths.get(newFilePath);
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        destination.setLastModified(source.lastModified());
    }



    public void deleteDirectory(File destination) throws IOException {
        logger.info("Deleting files in destination folder: " +  destination.getName());
        if(destination.isFile())
        {
            destination.delete();
        }
        else if(destination.listFiles().length == 0)
        {
            Files.delete(Paths.get(destination.getPath()));
        }
        else
        {
            for(File subfile: destination.listFiles())
            {
                if(subfile.isDirectory())
                {
                    deleteDirectory(subfile);
                }
                subfile.delete();
            }
        }
    }
}
