GRANT SELECT ON TABLE almac_crm_organizacion TO crm;
GRANT SELECT ON TABLE almac_piezasconprecio TO crm;
GRANT SELECT ON TABLE almac_crm_nom_comercial TO crm;
GRANT SELECT ON TABLE almac_crm_prodnomcom TO crm;
GRANT SELECT ON TABLE almac_crm_cliente TO crm;
GRANT SELECT ON TABLE c_bpartner TO crm;
GRANT SELECT ON TABLE ad_org TO crm;
GRANT SELECT ON TABLE ad_user TO crm;
GRANT SELECT ON TABLE m_warehouse TO crm;
GRANT SELECT ON TABLE m_product_category TO crm;
GRANT SELECT ON TABLE m_product TO crm;
GRANT SELECT ON TABLE compra_contenedor TO crm;
GRANT SELECT ON TABLE almac_crm_servicios TO crm;
GRANT SELECT ON TABLE almac_crm_pedidos TO crm;
GRANT SELECT ON TABLE c_bpartner TO crm;
ALTER FUNCTION almac_reserva_mercancia(character varying, character varying, character varying, character varying, character) SECURITY DEFINER;
ALTER FUNCTION almac_quita_mercancia(character varying) SECURITY DEFINER;
ALTER FUNCTION almac_crea_pedido(character varying, character varying, timestamp without time zone) SECURITY DEFINER;
ALTER FUNCTION almac_crea_flete(character varying, character varying, character varying, timestamp without time zone) SECURITY DEFINER;
ALTER FUNCTION almac_crm_creacabecera(character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying) SECURITY DEFINER;
ALTER FUNCTION almac_crm_crealinea(character varying, character varying, character varying, character varying, numeric, character varying, timestamp without time zone, character varying, numeric) SECURITY DEFINER;
ALTER FUNCTION almac_crm_completasalida(character varying) SECURITY DEFINER;
