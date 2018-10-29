CREATE TABLE tb_user (
  id            INT          NOT NULL PRIMARY KEY IDENTITY(1,1),
  idRh          VARCHAR(255) NOT NULL,
  first_name    VARCHAR(255) NOT NULL,
  last_name     VARCHAR(255) NOT NULL,
  last_modified DATETIME     NULL
);

INSERT INTO tb_user (idRh, first_name, last_name, last_modified) VALUES ('XPAX624','Jean','Dupond',GETDATE());
