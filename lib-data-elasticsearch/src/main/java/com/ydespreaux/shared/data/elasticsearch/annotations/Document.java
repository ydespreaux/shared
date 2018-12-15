package com.ydespreaux.shared.data.elasticsearch.annotations;



import com.ydespreaux.shared.data.elasticsearch.core.IndexTimeBasedSupport;

import java.lang.annotation.*;

/**
 * Cette annotation permet de définir un document à indexer dans elasticsearch utilisant un index time-based.
 *
 * @since 1.0.0
 * @author Yoann Despréaux
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {

    /**
     * Non de l'alias ou de l'index permettant d'effectuer des recherches dans elasticsearch
     * @return
     */
    String aliasOrIndex();

    /**
     * Type du document à indexer
     * @return
     */
    String type() default "";

    /**
     * Pattern définissant l'index en court. Ce pattern défini le nom de l'index time-based.
     *
     * @return
     */
    String indexPattern() default "";

    /**
     * La classe IndexTimeBasedSupport permet de générer le nom de l'index pour l'indexation de documents
     * en fonction de la date courante ainsi que le document à indexer.
     * @return
     */
    Class<? extends IndexTimeBasedSupport> indexTimeBasedSupport() default IndexTimeBasedSupport.class;

    /**
     * Création de l'index si ce dernier n'existe pas.
     * La création de l'index se base sur le template correspondant pour la configuration, le mapping etc...
     */
    boolean createIndex = true;

    /**
     * Path du fichier de configuration de l'index
     * @return
     */
    String indexPath() default "";


}

