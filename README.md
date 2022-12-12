# Encrypted Search Over FoundationDB - Java Implement


## 1. Environment

* Windows 10
* JDK8

## 2. Library

* FDB-Java-API 5.2.5
* [Clusion](https://github.com/encryptedsystems/Clusion)

## 3. Structure
### 3.1. fdb package: upload files (once)

* Class: [FDBService](src/main/java/edu/bu/fdb/FDBService.java)
* Functions: upload, download, clear files with FDB
* Test: [ServiceTest](src/test/java/edu/bu/fdb/ServiceTest.java)

### 3.2. search package: upload index (once) and update index (every time)
#### 3.2.1. RR2Lev
* Class: [ClusionRR2Lev](src/main/java/edu/bu/search/ClusionRR2Lev.java)
* Functions: create RR2Lev object; upload, download, clear, update index with FDB
* Test: [TestClusionRR2Lev](src/test/java/edu/bu/search/TestClusionRR2Lev.java)

#### 3.2.2. DlsD
* Class: [ClusionDlsD](src/main/java/edu/bu/search/ClusionDlsD.java)
* Functions: create DlsD object; upload, download, clear index with FDB
* Test: [TestClusionDlsD](src/test/java/edu/bu/search/TestClusionDlsD.java)

#### 3.2.3. MyRR2Lev
* Class: [MyRR2Lev](src/main/java/edu/bu/search/MyRR2Lev.java)
* Functions: store the necessary parts in the Clusion algorithm which needs to upload; It also provides functions to convert between MultiMap and Map

### 3.3. util package: analysis
* Class: [Analysis](src/main/java/edu/bu/util/Analysis.java)
* Functions: compute the rate between the indexes and files

### 3.4. Startup
* Class: [Startup](src/main/java/edu/bu/Startup.java)
* Functions: startup class in jar. Run the jar package of the project will run the class automatically.
* Statement: It will use DlsD because it creates indexes smaller than RR2Lev

## 4. Test on Massachusetts Open Cloud
* Output: [Output](output.txt)