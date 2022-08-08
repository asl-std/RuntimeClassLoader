<h1 align="center" style="display: block; font-size: 2.5em; font-weight: bold; margin-block-start: 1em; margin-block-end: 1em;">
<a name="logo" href="https://www.aregtech.com"><img align="center" src="https://avatars.githubusercontent.com/u/50813823?s=200&v=4" alt="Ãs Šecond Łife std. logo" style="width:128px;height:100%;"/></a>
  <br><br><strong>RuntimeClassLoader</strong>
</h1>

<br>

## Introduction![pin](https://user-images.githubusercontent.com/89454799/181916421-42971bd6-95b4-4c5f-91ab-04de306f1aa5.svg)
<p><strong>RuntimeClassLoader</strong> is a useful utility library for interacting with an inaccessible environment in Java.</p>

_At the moment, the library supports the following OS and Java Environment:_
<table>
  <tr>
    <td nowrap><strong>Supported OS</strong></td>
    <td>Windows 10 / 11, Linux (not sure), Mac OS (not sure)</td>
  </tr>
  <tr>
    <td nowrap><strong>Supported Java</strong></td>
    <td>SE JDK 8, 16, 17</td>
  </tr>
</table>


[![GitHub license](https://img.shields.io/github/license/asl-std/RuntimeClassLoader?style=plastic)](https://github.com/asl-std/OAuth2Discord/blob/release/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/asl-std/RuntimeClassLoader?style=plastic)](https://github.com/asl-std/OAuth2Discord/issues)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/asl-std/RuntimeClassLoader/Build?style=plastic)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/asl-std/RuntimeClassLoader/Maven%20Central%20deploy?style=plastic)
![Maven Central](https://img.shields.io/maven-central/v/ru.aslcraft.runtimeclassloader?style=plastic)

## Usage![pin](https://user-images.githubusercontent.com/89454799/181916421-42971bd6-95b4-4c5f-91ab-04de306f1aa5.svg)

### How to load the Maven library in runtime

<details><summary><b>Show instructions</b></summary>

1. Download compiled lib from releases or just download sources and compile it-self

2. Create an instance of the ```MavenURL``` object and specify arguments based on their name (P.S. the default repository is _Repository.Central_):
![example_1_0](https://user-images.githubusercontent.com/89454799/181917265-269141b8-526e-41ef-b3f0-2d4578608ca1.png)

3. Create an instance of the ```MavenClassLoader``` object by specifying the created reference to the Maven library in the arguments:
![example_1_1](https://user-images.githubusercontent.com/89454799/181917379-8d6c9fb0-9471-4c62-b595-7fbff78292a2.png)

4. Then invoke the method ```MavenClassLoader#loadClasses()``` and wait for some time, while the libraries classes will be loaded:
![example_1_2](https://user-images.githubusercontent.com/89454799/181917542-8bcba9bd-9241-4bc1-8301-cbc78020b8d1.png)

<strong>Also,</strong> if necessary, you can catch an error when creating an instance of ```MavenClassLoader```:
![example_1_0](https://user-images.githubusercontent.com/89454799/181918599-7ede16f0-de52-4efd-90ce-1b2f6dc3c313.png)

</details>

### How to use the custom Reflection class

<details><summary><b>Show instructions</b></summary>

1. Download compiled lib from releases or just download sources and compile it-self
  
2. Get an instance of an abstract object ```Reflection``` by calling the ```ReflectionFactory#createReflection()``` method:
![example_2_0](https://user-images.githubusercontent.com/89454799/182034023-b420717f-0631-4061-954c-d8ab272ff6dc.png)

</details>
