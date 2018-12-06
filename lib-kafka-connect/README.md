Librairie Kafka Connect
=========================

Afin de faciliter le déploiement d'un module Kafka Connect, la librairie lib-kafka-connect a été développé afin de pouvoir configurer un cluster Kafka Connect à l'aide d'une API Spring Boot bénéficiant des avantages de spring boot.
Cette librairie est compatible avec les versions 2.x de Spring Boot.

Cette librairie permet de:
- Configurer le worker du cluster Kafka-Connect à l'aide du fichier de propriétés spring boot
- Configurer les connecteurs à l'aide du fichier de propriétés spring boot
- Se connecter à un cluster Kafka-Connect existant (via une image Kafka-Connect)
- Embarquer un cluster Kafka-Connect fonctionnant en mode autonome ou en mode distribué
- Définit un endpoint pour actuator permettant de vérifier l'état du cluster et le statut des connecteurs


Versions
-----------

|   lib-kafka-connect | SpringBoot  |   Kafka   |   Confluent   |
|:-------------------:|:-----------:|:---------:|:-------------:|
|   1.0.1             | 2.x         |   1.1.0   |   4.1.0       |
|   1.0.0             | 2.x         |   1.1.0   |   4.1.0       |

## Add the Maven dependency

```xml
<dependency>
    <groupId>com.ydespreaux.shared.kafka</groupId>
    <artifactId>lib-kafka-connect</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Changelog

### [1.0.2]

#### Changed

- Ajout du mode DISTRIBUTED pour un cluster kafka-connect embarqué pour le mode distribué (ce nouveau mode remplace le mode DISTRIBUED, ce dernier est devenu déprécié)

### [1.0.1]

#### Added

- Ajout des propriétés suivante permettant la configuration SSL du cluster en mode embarqué :
    - spring.kafka-connect.cluster.security-protocol (SSL ou PLAINTEXT)
    - spring.kafka-connect.cluster.ssl.key-password
    - spring.kafka-connect.cluster.ssl.keystore-location
    - spring.kafka-connect.cluster.ssl.keystore-password
    - spring.kafka-connect.cluster.ssl.truststore-location
    - spring.kafka-connect.cluster.ssl.truststore-password    
- Prise en compte du schéma registry en https

### [1.0.0]

#### Added

- Configuration d'un cluster embarqué ou distant
- Configuration des connecteurs
- Ajout de l'api rest (en embarqué)
- Ajout de la description swagger (en embarqué)
- Ajout de l'état du cluster au health check d'actuator
