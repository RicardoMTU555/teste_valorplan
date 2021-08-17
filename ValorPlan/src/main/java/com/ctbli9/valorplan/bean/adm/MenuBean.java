package com.ctbli9.valorplan.bean.adm;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

@ManagedBean(name="menuBean")
@RequestScoped
public class MenuBean {
	
	public String direciona(String pagina) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		//context.getSessionMap().remove("consultaGrupoBean");
		
		return pagina + "?faces-redirect=true";
	}

}
