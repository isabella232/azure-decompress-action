Azure Decompress Action
========================

Azure decompress Action plugin decompress gz files from a container on Azure Storage Blob service into another container. 

Plugin Configuration
---------------------

| Configuration | Required | Default | Description |
| :------------ | :------: | :----- | :---------- |
| **accountName** | **Y** | N/A | This configuration specifies the Microsoft Azure Storage account name. |
| **accountKey** | **Y** | N/A | This configuration specifies the account key for the specified Azure Storage account name. |
| **inputContainer** | **Y** | N/A | This configuration specifies the container with input gz files. |
| **outputContainer** | **Y** | N/A| This configuration specifies the container for output decompressed files. |


Usage Notes
-----------

The plugin may be used at the start of a pipeline run to decompress gz files on a container on Azure Blob service into another container. It can be expanded to take other compressed input format in future.