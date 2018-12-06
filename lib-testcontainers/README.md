
Tests d'intégration avec testcontainers
=========================

Cette librairie implémente des containeurs docker pour les tests d'intégrations.
Les types de containeurs disponibles sont:
* MySQL
* Cassandra
* Elasticsearch
* Kafka
* Microsoft SQL Server 2017 (developer edition)

Versions
-----------

|   lib-core-test-containers |   testcontainers |
|:--------------------------:|:----------------:|
|   1.0.0                    |       1.8.3      |

### MySQL

##### Add the Maven dependency

```xml
<dependency>
    <groupId>com.ydespreaux.shared.testcontainers</groupId>
    <artifactId>lib-testcontainers-mysql</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

##### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer();
}    
```
Par défaut l'image MySQL utilisée est 'mysql:5.7.22'.
L'url de l'image et la version sont paramétrables. Ci-dessous un exemple permettant d'utiliser la version 5.7.21 de l'image.
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21");
}    
```

Lorsque le container docker a démarré, les propriétés suivantes sont initialisées dans le contexte de spring boot:

|   Propriété spring boot               |      Valeur par défaut                            |   Remarques                                                           |
|:-------------------------------------:|:-------------------------------------------------:|:---------------------------------------------------------------------:|
|   spring.datasource.url               | jdbc:mysql://localhost:33001/db_test?useSSL=false | Le port est généré aléatoirement                                      | 
|   spring.datasource.driver-class-name | com.mysql.jdbc.Driver                             |                                                                       | 
|   spring.datasource.username          | db_user_test                                      |                                                                       | 
|   spring.datasource.password          | ***********                                       | Le mot de passe de l'utilisateur est généré automatiquement par défaut| 
|   spring.datasource.platform          | mysql                                             |                                                                       | 

La méthode withRegisterSpringbootProperties(boolean) permet d'initialiser ou non le contexte spring boot des propriétés ci-dessus.

L'exemple ci-dessous permet de ne pas mettre à jour les propriétés spring boot :
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21")
        .withRegisterSpringbootProperties(false);
}    
```

Le nom des propriétés spring boot peuvent être modifiées à l'aide des méthodes suivantes:

| Méthode                           | Description                                                                   | Valeur par défaut                     |
|:---------------------------------:|:-----------------------------------------------------------------------------:|:-------------------------------------:|
| withDriverClassSystemProperty     | Modifie la propriété correspondant au driver MySQL                            | spring.datasource.driver-class-name   |
| withUrlSystemProperty             | Modifie la propriété correspondant à l'URL                                    | spring.datasource.url                 |
| withUsernameSystemProperty        | Modifie la propriété correspondant au nom de l'utilisateur MySQL              | spring.datasource.username            |
| withPasswordSystemProperty        | Modifie la propriété correspondant au mot de passe de l'utilisateur MySQL     | spring.datasource.password            |
| withPlatformSystemProperty        | Modifie la propriété correspondant à la platform                              | spring.datasource.platform            |

Paramétrage de la base de données MySQL:

| Méthodes                      | Description                                       | Valeur par défaut         |
|:-----------------------------:|:-------------------------------------------------:|:-------------------------:|
| withDatabaseName              | Modifie le nom de la base de données              | db_test                   |
| withUsername                  | Modifie le nom de l'utilisateur MySQL             | db_user_test              |
| withPassword                  | Modifie le mot de passe de l'utilisateur MySQL    | Généré automatiquement    |
| withRootPassword              | Modifie le mot de passe ROOT                      | Généré automatiquement    |
| withConfigurationOverride     | Modifie le fichier de configuration MySQL         |                           |

Initialisation du schéma de la base de données:

Exécution d'un script sql :

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21")
        .withMySqlInit("mysql-init/mysql-init.sql");
}    
```

Le fichier mysql-init.sql (se trouvant dans le répertoire mysql-init) sera exécuté après le démarrage du container.
Cette fonctionnalité est disponible à partir de la version 1.0.2 de la librairie.

Exécution d'un ensemble de scripts sql:
Cette fonctionnalité est diponible depuis la version 1.1.0 de la librairie. 

Exemple permettant d'exécuter 2 scripts sql:
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21")
        .withMySqlInit("mysql-init/mysql-init-1.sql")
        .withMySqlInit("mysql-init/mysql-init-2.sql");
}    
```
Exemple permettant d'exécuter tous les scripts sql contenu dans le répertoire 'mysql-init':
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final MySQLContainer mySqlContainer = new MySQLContainer("5.7.21")
        .withMySqlInitDirectory("mysql-init");
}    
```
 

### Cassandra

##### Add the Maven dependency

```xml
<dependency>
    <groupId>com.ydespreaux.shared.testcontainers</groupId>
    <artifactId>lib-testcontainers-cassandra</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

##### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITCassandraTest {

    @ClassRule
    public static final CassandraContainer mySqlContainer = new CassandraContainer();
}    
```
Par défaut l'image cassandra utilisée est 'cassandra:3.11'.
L'url de l'image et la version est paramétrable. Ci dessous un exemple permettant d'utiliser la version 3 de l'image.
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITCassandraTest {

    @ClassRule
    public static final CassandraContainer mySqlContainer = new CassandraContainer("3");
}    
```

Lorsque le container docker a démarré, les propriétés suivantes sont initialisées dans le contexte de spring boot:

|   Propriété spring boot                   |      Valeur par défaut    |   Remarques                               |
|:-----------------------------------------:|:-------------------------:|:-----------------------------------------:|
|   spring.data.cassandra.contact-points    | localhost                 | Le host correspond au hostname de docker  | 
|   spring.data.cassandra.port              | 33001                     | Le port est généré aléatoirement          | 

La méthode withRegisterSpringbootProperties(boolean) permet d'initialiser ou non le contexte spring boot des propriétés ci-dessus.

Le nom des propriétés spring boot peuvent être modifiées à l'aide des méthodes suivantes:

| Méthode                           | Description                                           | Valeur par défaut         |
|:---------------------------------:|:-----------------------------------------------------:|:-------------------------:|
| withContactPointsSystemProperty   | Modifie la propriété des uris des cluster cassandra   | Le hostname de docker     |
| withCassandraPortSystemProperty   | Modifie la propriété correspondant au port cassandra  | Port généré aléatoirement |

Initialisation du schéma de la base de données:

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMySQLTest {

    @ClassRule
    public static final CassandraContainer cassandraContainer = new CassandraContainer()
        .withCqlScriptDirectory("db-schema");

}    
```

La méthode withCqlScriptDirectory(...) prend un paramètre le chemin d'un répertoire. L'ensemble des scripts cql seront exécutés après le démarrage du container. 
Cette fonctionnalité est disponible à partir de la version 1.0.2 de la librairie.

### Elasticsearch

##### Add the Maven dependency

```xml
<dependency>
    <groupId>com.ydespreaux.shared.testcontainers</groupId>
    <artifactId>lib-testcontainers-elasticsearch</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

##### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITElasticsearchTest {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer();
}    
```
Par défaut l'image elasticsearch utilisée est 'elasticsearch/elasticsearch:5.6.8'.
L'url de l'image et la version est paramétrable. Ci dessous un exemple permettant d'utiliser la version 5.6.9 de l'image.
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITElasticsearchTest {

    @ClassRule
    public static final ElasticsearchContainer elasticContainer = new ElasticsearchContainer("5.6.9");
}    
```
A noter que la sécurité est désactivée.

Lorsque le container docker a démarré, les propriétés suivantes sont initialisées dans le contexte de spring boot:

|   Propriété spring boot               |      Valeur par défaut    |   Remarques                       |
|:-------------------------------------:|:-------------------------:|:---------------------------------:|
|   spring.elasticsearch.jest.uris      | http://localhost:33001    | Le port est généré aléatoirement  |                                     | 

La méthode withRegisterSpringbootProperties(boolean) permet d'initialiser ou non le contexte spring boot des propriétés ci-dessus.

Le nom des propriétés spring boot peuvent être modifiées à l'aide des méthodes suivantes:

| Méthode                    | Description                                                  |
|:--------------------------:|:------------------------------------------------------------:|
| withUrisSystemProperty     | Modifie la propriété correspondant aux uris d'elasticsearch  |

### Kafka

#### Add the Maven dependency

```xml
<dependency>
    <groupId>com.ydespreaux.shared.testcontainers</groupId>
    <artifactId>lib-testcontainers-kafka</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```

#### Container Kafka

##### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer();
}    
```
Par défaut l'image Kafka utilisée est 'confluentinc/cp-kafka:3.3.1'.

La version est paramétrable. Ci dessous un exemple permettant d'utiliser la version 4.0.0 de l'image.
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer("4.0.0");
}    
```

###### Activation du schéma registry.

Par défaut, le schéma registry n'est pas actif. Ci dessous un exemple permettant d'activer le schema registry.

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer().withSchemaRegistry(true);
}    
```

###### Configuration du container.

Lorsque le container docker a démarré, les propriétés suivantes sont initialisées dans le contexte de spring boot:

|   Propriété spring boot                       |      Valeur par défaut    |   Remarques                                                                                                                       |
|:---------------------------------------------:|:-------------------------:|:---------------------------------------------------------------------------------------------------------------------------------:|
|   spring.kafka.bootstrap-servers              | localhost:33001           | Le port du broker est généré aléatoirement                                                                                        | 
|   spring.kafka.properties.schema.registry.url | http://localhost:33002    | Le port du schéma registry est généré aléatoirement. Cette propriété est initialisée seulement si le schéma registry est activé.  | 

La méthode withRegisterSpringbootProperties(boolean) permet d'initialiser ou non le contexte spring boot des propriétés ci-dessus.

Le nom des propriétés spring boot peuvent être modifiées à l'aide des méthodes suivantes:

| Méthode                           | Description                                                       | Valeur par défaut                             |
|:---------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------:|
| withBrokerServersSystemProperty   | Modifie la propriété correspondante aux uris des brokers          | spring.kafka.bootstrap-servers                |
| withSchemaRegistrySystemProperty  | Modifie la propriété correspondante à l'url du schema registry    | spring.kafka.properties.schema.registry.url   |

###### Accès aux url.

| Méthode                   | Description                               | Valeur par défaut     |
|:-------------------------:|:-----------------------------------------:|:---------------------:|
| getBootstrapServers()     | Retourne l'url des brokers kafka          | localhost:33001       |
| getZookeeperServer()      | Retourne l'url du serveur zookeeper       | localhost:33001       |
| getZookeeperConnect()     | Retourne l'url local du serveur zookeeper | zookeeper:33001       |

###### Configuration des topics (version 1.1.2).

Ajout de la méthode withTopic permettant de créer un topic prenant le nom du topic, le nombre de partitions et le type de topic compacté ou non.  

| Méthode                               | Description                                           |
|:-------------------------------------:|:-----------------------------------------------------:|
| withTopic(topic, partitions, compact) | Création d'un topic à l'initialisation du container   |
| withTopic(TopicConfiguration)         | Création d'un topic à l'initialisation du container   |

Exemple:

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer()
        .withTopic("topic1-non-compact", 3, false)
        .withTopic("topic2-compact", 3, true);
}    
```


##### Container Kafka Connect

##### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaConnectContainer kafkaContainer = new ConfluentKafkaConnectContainer();
}    
```
Par défaut l'image Kafka connect utilisée est 'confluentinc/cp-kafka-connect:4.1.0'. La compatibilité des messages Kafka est fixée avec la version 0.11.0 par défaut. 

La version est paramétrable. Ci dessous un exemple permettant d'utiliser la version 4.0.0 de l'image.
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITKafkaTest {

    @ClassRule
    public static final ConfluentKafkaContainer mySqlContainer = new ConfluentKafkaContainer("4.0.0");
}    
```

###### Configuration du container.

Lorsque le container docker a démarré, les propriétés suivantes sont initialisées dans le contexte de spring boot:

|   Propriété spring boot                       |      Valeur par défaut    |   Remarques                                                                                                                       |
|:---------------------------------------------:|:-------------------------:|:---------------------------------------------------------------------------------------------------------------------------------:|
|   spring.kafka.bootstrap-servers              | localhost:<port>          | Le port du broker est généré aléatoirement                                                                                        | 
|   spring.kafka.properties.schema.registry.url | http://localhost:<port>   | Le port du schéma registry est généré aléatoirement. Cette propriété est initialisée seulement si le schéma registry est activé.  | 
|   spring.kafka-connect.rest-app               | http://localhost:<port>   | Le port du l'application Rest est généré aléatoirement.                                                                           | 

La méthode withRegisterSpringbootProperties(boolean) permet d'initialiser ou non le contexte spring boot des propriétés ci-dessus.

Le nom des propriétés spring boot peuvent être modifiées à l'aide des méthodes suivantes:

| Méthode                           | Description                                                       | Valeur par défaut                             |
|:---------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------:|
| withBrokerServersSystemProperty   | Modifie la propriété correspondante aux uris des brokers          | spring.kafka.bootstrap-servers                |
| withSchemaRegistrySystemProperty  | Modifie la propriété correspondante à l'url du schema registry    | spring.kafka.properties.schema.registry.url   |
| withRestAppSystemProperty         | Modifie la propriété correspondante à l'url dl'application REST   | spring.kafka-connect.rest-app                 |

Méthode d'initialisation du worker:

| Méthode                           | Description                                                       | Valeur par défaut                             |
|:---------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------:|
| withKeyConverter                  | Définit le type de "Converter" des clés des messages              | org.apache.kafka.connect.json.JsonConverter   |
| withValueConverter                | Définit le type de "Converter" des valeurs des messages           | org.apache.kafka.connect.json.JsonConverter   |
| withPlugins                       | Ajout des plugins                                                 | Aucun plugin par défaut                       |

Dans le cas d'un converter Avro le schéma registry est automatiquement configuré si ce dernier a été activé.

Méthode d'initialisation du Herder en mode standalone:

| Méthode                           | Description                                                       | Valeur par défaut                             |
|:---------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------:|
| withOffsetStorageFilename         | Nom du fichier de stockage des offsets                            | connect-offsets-file.txt                      |

Méthode d'initialisation du Herder en mode distribué:

| Méthode                           | Description                                                       | Valeur par défaut                             |
|:---------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------:|
| withGroupId                       | Nom du groupe                                                     | kafka-connect-group                           |
| withConfigStorageTopic            | Nom du topic de configuration                                     | connect-config                                |
| withOffsetStorageTopic            | Nom du topic des offsets                                          | connect-offsets                               |
| withOffsetStoragePartition        | Nombre de partitions du topic des offsets                         | 3                                             |
| withStatusStorageTopic            | Nom du topic des status                                           | connect-status                                |
| withStatusStoragePartition        | Nombre de partitions du topic des status                          | 3                                             |


###### Accès aux url.

| Méthode                   | Description                               | Valeur par défaut         |
|:-------------------------:|:-----------------------------------------:|:-------------------------:|
| getBootstrapServers()     | Retourne l'url des brokers kafka          | localhost:<port>          |
| getZookeeperServer()      | Retourne l'url du serveur zookeeper       | localhost:<port>          |
| getZookeeperConnect()     | Retourne l'url local du serveur zookeeper | zookeeper:<port>          |
| getRestAppServers()       | Retourne l'url de l'application REST      | http://localhost:<port>   |

### Container Microsoft SQL Server 2017 Developer edition

#### Licence

L'image MS SQL Server utilisée par défaut est 'microsoft/mssql-server-linux:2017-CU9'.

Elle permet d'utiliser la dernière version de "Microsoft SQL Server 2017 Developer edition".

L'utilisation de SQL Server est soumise à licence. La variable d'environnement "ACCEPT_EULA" valorisée à "Y" vous permet d'exprimer que vous possédez une licence valide pour l'édition et la version de SQL Server que vous vous apprêtez à utiliser.

Par défaut c'est l'édition "Developer" qui est déployée, réservée au développement et aux tests.

Extrait (https://hub.docker.com/r/microsoft/mssql-server-linux/)
"
SQL Server Developer edition lets developers build any kind of application on top of SQL Server. It includes all the functionality of Enterprise edition, but is licensed for use as a development and test system, not as a production server. SQL Server Developer Edition cannot be used in a production environment. The SQL Server 2017 Developer Edition license terms are located here.
"

**L'acceptation de la licence implique la présence du fichier "resources/container-license-acceptance.txt" contenant le terme "microsoft/mssql-server-linux:<version>" (ex : microsoft/mssql-server-linux:2017-CU9)**

##### Quick Start

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMSSQLTest {

    @ClassRule
    public static final MSSQLServerContainer mssqlContainer = new MSSQLServerContainer();
}    
```

La version de l'image est paramétrable. Ci-dessous un exemple permettant d'utiliser la version 2017-CU9 de l'image.

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMSSQLTest {

    @ClassRule
    public static final MSSQLContainer mssqlContainer = new MSSQLContainer("2017-CU9");
}    
```

Lorsque le container docker a démarré, les propriétés suivantes sont initialisées dans le contexte de spring boot:

|   Propriété spring boot               |      Valeur par défaut                                |   Remarques                                                             |
|:-------------------------------------:|:-----------------------------------------------------:|:-----------------------------------------------------------------------:|
|   spring.datasource.url               | jdbc:sqlserver://localhost:33001;databaseName=db_test | Le port est généré aléatoirement                                        | 
|   spring.datasource.driver-class-name | com.microsoft.sqlserver.jdbc.SQLServerDriver          |                                                                         | 
|   spring.datasource.username          | SA                                                    | Par défaut l'utilisateur est SA                                         | 
|   spring.datasource.password          | ***********                                           | Le password par défaut de l'utilisateur SA de l'image docker est utilisé| 
|   spring.datasource.platform          | sqlserver                                             |                                                                         | 

Pour s'authentifier avec un utilisateur autre que SA il vous faudra ajouter un script SQL permettant d'ajouter cet utilisateur.

La méthode withRegisterSpringbootProperties(boolean) permet d'initialiser ou non le contexte spring boot des propriétés ci-dessus.

L'exemple ci-dessous permet de ne pas mettre à jour les propriétés spring boot :
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMSSQLTest {

    @ClassRule
    public static final MSSQLContainer mssqlContainer = new MSSQLContainer()
        .withRegisterSpringbootProperties(false);
}    
```

Le nom des propriétés spring boot peuvent être modifiées à l'aide des méthodes suivantes:

| Méthode                           | Description                                                                   | Valeur par défaut                     |
|:---------------------------------:|:-----------------------------------------------------------------------------:|:-------------------------------------:|
| withDriverClassSystemProperty     | Modifie la propriété correspondant au driver MSSQL                            | spring.datasource.driver-class-name   |
| withUrlSystemProperty             | Modifie la propriété correspondant à l'URL                                    | spring.datasource.url                 |
| withUsernameSystemProperty        | Modifie la propriété correspondant au nom de l'utilisateur MSSQL              | spring.datasource.username            |
| withPasswordSystemProperty        | Modifie la propriété correspondant au mot de passe de l'utilisateur MSSQL     | spring.datasource.password            |
| withPlatformSystemProperty        | Modifie la propriété correspondant à la platform                              | spring.datasource.platform            |

Paramétrage de la base de données MSSQL:

| Méthodes                      | Description                                       | Valeur par défaut             |
|:-----------------------------:|:-------------------------------------------------:|:-----------------------------:|
| withDatabaseName              | Modifie le nom de la base de données              | db_test                       |
| withUsername                  | Modifie le nom de l'utilisateur MSSQL             | SA                            |
| withPassword                  | Modifie le mot de passe de l'utilisateur MSSQL    | mot de passe SA par défaut    |
| withDatabaseAutoCreate        | Si positionné une base de données est créée avec  | 
                                | le nom de la base de données (ex: db_test)        | 

Initialisation du schéma de la base de données:

Exécution d'un script sql :

```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMSSQLTest {

    @ClassRule
    public static final MSSQLContainer mssqlContainer = new MSSQLContainer()
        .withMsSqlInit("mssql-init/mssql-init.sql");
}    
```

Le fichier mssql-init.sql (se trouvant dans le répertoire mssql-init) sera exécuté après le démarrage du container.
Cette fonctionnalité est disponible à partir de la version 1.1.2 de la librairie.

Exécution d'un ensemble de scripts sql:
Cette fonctionnalité est disponible depuis la version 1.1.2 de la librairie. 

Exemple permettant d'exécuter 2 scripts sql :
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMSSQLTest {

    @ClassRule
    public static final MSSQLContainer mssqlContainer = new MSSQLContainer()
        .withMsSqlInit("mssql-init/mssql-init.sql")
        .withMsSqlInit("mssql-init/script-1.sql");
}    
```
Exemple permettant d'exécuter tous les scripts sql contenus dans le répertoire 'mysql-init':
```java
@RunWith(SpringJUnit4ClassRunner.class)
public class ITMSSQLTest {

    @ClassRule
    public static final MSSQLContainer mssqlContainer = new MSSQLContainer()
        .withMsSqlInitDirectory("mysql-init");
}    
```

## Changelog

### [1.1.3]

#### Changed

#### Added

### [1.1.2]

#### Changed

- Upgrade de la librairie org.testcontainers en version 1.8.3

#### Added

- [lib-testcontainers-mssql] : 
    - Ajout du container MSSQLServerContainer basée sur l'image docker microsoft/mssql-server-linux
- [lib-testcontainers-kafka] :
    - Création de topics au démarrage du conteneur.

### [1.1.1]

#### Changed

- [lib-testcontainers-elasticsearch] : 
    - Modification de l'url de base de l'image elasticsearch: elasticsearch/elasticsearch
- [lib-testcontainers-kafka] : 
    - Ajout de la méthode withFormatMessageVersion(String version) permettant de fixer la version de compatibilité des messages Kafka

#### Added
- [lib-testcontainers-kafka] : 
    - Ajout du container ConfluentKafkaConnectContainer basée sur l'image docker confluentinc/cp-kafka-connect

### [1.1.0]

#### Changed

- Mise à jour de la version org.testcontainers : 1.8.0
- [lib-testcontainers-kafka] : 
    - Suppression de la dépendance avec la librairie kafka-schema-registry
- [lib-testcontainers-mysql] : 
    - Ajout de la méthode withMySqlInitDirectory permettant d'ajouter un ensemble de scripts d'initialisation contenus dans un répertoire

#### Added
- [lib-testcontainers-kafka] : 
    - Ajout de l'image docker confluentinc/cp-schema-registry

### [1.0.2]

#### Changed

- [lib-testcontainers-cassandra] : Execution de scripts d'initialisation du schéma cassandra
- [lib-testcontainers-mysql] : Execution d'un script d'initialisation de la base de données MySQL

### [1.0.1]

#### Added

- Ajout du container Kafka

### [1.0.0]

#### Added

- Ajout du container MySQL
- Ajout du container Cassandra
- Ajout du container Elasticsearch