/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package co.cask.hydrator.plugin.batch;


import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Macro;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.cdap.api.plugin.PluginConfig;
import co.cask.cdap.etl.api.action.Action;
import co.cask.cdap.etl.api.action.ActionContext;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobInputStream;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.zip.GZIPInputStream;

/**
 * Action to decompress gz files from a container on Azure Storage Blob service into another container
 */

@Plugin(type = Action.PLUGIN_TYPE)
@Name("AzureDecompress")
@Description("Action to decompress gz files from a container on Microsoft Azure Blob service into another container.")
public class AzureDecompressAction extends Action{
  private static final Logger LOG = LoggerFactory.getLogger(AzureDecompressAction.class);
  private static final int BUFFER_SIZE = 4096;

  private AzureDecompressActionConfig config;

  public AzureDecompressAction(AzureDecompressActionConfig config) {
    this.config = config;
  }

  @Override
  public void run(ActionContext context) throws Exception {
    /**
     * Stores the storage connection string.
     */
    final String storageConnectionString = "DefaultEndpointsProtocol=https;"
      + "AccountName=" + config.accountName + ";"
      + "AccountKey=" + config.accountKey;

    // Setup the cloud storage account.
    CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);

    // Create a blob service client
    CloudBlobClient blobClient = account.createCloudBlobClient();

    try {
      CloudBlobContainer containerGZip = blobClient.getContainerReference(config.inputContainer);
      CloudBlobContainer containerUnzip = blobClient.getContainerReference(config.outputContainer);

      //Report error if the input contain does not exist
      if (!containerGZip.exists()) {
        LOG.error("The input container {} with gz files does not exist.", config.inputContainer);
        throw new Exception("The given input container " + config.inputContainer + " does not exist.");
      }

      //Create the output container if it doesn't exist
      if (containerUnzip.createIfNotExists()) {
        LOG.info("Create output container {} successfully.", config.outputContainer);
      } else {
        LOG.info("The output container {}  already exists.", config.outputContainer);
      }

      //Loop through the given input container
      for (ListBlobItem blobItem : containerGZip.listBlobs()) {

        //If the item is a blob, not a virtual directory
        if (blobItem instanceof CloudBlockBlob) {
          String path = blobItem.getUri().getPath();

          //Process gz only
          if (path.endsWith(".gz")) {
            //Get unzipped blob name
            String outputName = path.substring(path.lastIndexOf('/')+1, path.lastIndexOf(".gz"));

            //Get a reference to a blob in the container
            CloudBlockBlob unzipBlob= containerUnzip.getBlockBlobReference(outputName);
            BlobOutputStream blobOutputStream = unzipBlob.openOutputStream();

            BlobInputStream blobInputStream = ((CloudBlockBlob) blobItem).openInputStream();
            GZIPInputStream gzis = new GZIPInputStream(blobInputStream);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len = 0;

            //Extract compressed content.
            while ((len = gzis.read(buffer)) > 0) {
              blobOutputStream.write(buffer, 0, len);
            }

            //Release resources.
            blobInputStream.close();
            blobOutputStream.close();
            gzis.close();
            buffer = null;
          }
        }
      }

    } catch (Exception e) {
      LOG.error("Error when decompressing gz in the container {} to the container {}", config.inputContainer, config.outputContainer);
      throw e;
    }
  }


  /**
   *  Config for the action to decompress gz files from a container on Azure Storage Blob service into another container
   */
  public class AzureDecompressActionConfig extends PluginConfig {
    @Description("The Microsoft Azure Storage account name.")
    @Macro
    private String accountName;

    @Description("The account key for the specified Azure Storage account name.")
    @Macro
    private String accountKey;

    @Description("The container with input gz files")
    @Macro
    private String inputContainer;

    @Description("The container for output decompressed files")
    @Macro
    private String outputContainer;

    public AzureDecompressActionConfig(String accountName, String accountKey, String inputContainer, String outputContainer) {
      this.accountName = accountName;
      this.accountKey = accountKey;
      this.inputContainer = inputContainer;
      this.outputContainer = outputContainer;
    }
  }

}
