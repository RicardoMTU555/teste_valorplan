package com.ctbli9.valorplan.modelo;

public class RelatorioBI {
	private int cdCampoBI;
	private int nrAno;
	private String dsCampoBI;
	
	public int getCdCampoBI() {
		return cdCampoBI;
	}
	public void setCdCampoBI(int cdCampoBI) {
		this.cdCampoBI = cdCampoBI;
	}
	
	public int getNrAno() {
		return nrAno;
	}
	public void setNrAno(int nrAno) {
		this.nrAno = nrAno;
	}
	
	public String getDsCampoBI() {
		if (dsCampoBI.length() > 60)
			dsCampoBI = dsCampoBI.substring(0, 60);
		return dsCampoBI;
	}
	public void setDsCampoBI(String dsCampoBI) {
		if (dsCampoBI == null)
			dsCampoBI = "";
		this.dsCampoBI = dsCampoBI;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cdCampoBI;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelatorioBI other = (RelatorioBI) obj;
		if (cdCampoBI != other.cdCampoBI)
			return false;
		return true;
	}
}
