CREATE TABLE tb_workstation (
  id            INT          NOT NULL PRIMARY KEY IDENTITY(1, 1),
  name          VARCHAR(255) NOT NULL,
  serial_number    VARCHAR(255) NOT NULL
);

INSERT INTO tb_workstation (name, serial_number) VALUES ('WS10002','WS-1234-5678');
