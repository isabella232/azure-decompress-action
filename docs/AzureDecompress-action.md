# AzureDecompress Action

Description
-----------

Decompress gz files from a container on Azure Storage Blob service into another container.

Use Case
--------

This action may be used at the start of a pipeline run to decompress gz files on a container on Azure Blob service into another container.

Properties
----------

**accountName:** The Microsoft Azure Storage account name. (Macro-enabled)

**accountKey:** The account key for the specified Azure Storage account name. (Macro-enabled)

**inputContainer:** The container with input gz files. (Macro-enabled)

**outputContainer:** The container for output decompressed files. (Macro-enabled)

Example
-------

This example decompresses gz files on the container 'testInput' into the container 'testOutput'.

    {
        "name": "AzureDecompressAction",
        "type": "action",
        "properties": {
            "accountName": "myStorageAccountNameInAzure",
            "accountKey": "myStorageAccountKey+b2EN6SVpcg==",
            "inputContainer": "testInput",
            "outputContainer": "testOutput"
        }
    }
