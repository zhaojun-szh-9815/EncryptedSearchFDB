# Encrypted Search Over FoundationDB - Java Implement


## 1. Environment

* Windows 10
* JDK8

## 2. Library

* FDB-Java-API 5.2.5
* [Clusion](https://github.com/encryptedsystems/Clusion)

## 3. Upload Files (Once)

* Class: [FDBService](src/main/java/edu/bu/fdb/FDBService.java)
* Functions: upload, download, clear files with FDB
* Test: [ServiceTest](src/test/java/edu/bu/fdb/ServiceTest.java)

## 4. Upload Index (Once) and Update Index (every time)
* Class: [ClusionRR2Lev](src/main/java/edu/bu/search/ClusionRR2Lev.java)
* Functions: upload, download, clear, update index with FDB
* Test: [TestClusionRR2Lev](src/test/java/edu/bu/search/TestClusionRR2Lev.java)