<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:c="http://www.esei.uvigo.es/dai/hybridserver">

<xsl:output method="html" indent="yes" encoding="utf-8"/>

	<xsl:template match="/">
	<xsl:text disable-output-escaping="yes">&lt;!DOCTYPE HTML&gt;</xsl:text>
		<html>
			<head>
				<title>HybridServer</title>
			</head>
			<body>
				<div id="container">
					<h1>Configuración</h1>
					<div id="config">
						<xsl:apply-templates select="c:connection"/>
						<xsl:apply-templates select="c:database"/>
						<xsl:apply-templates select="c:servers"/>
					</div>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="c:connection">
	<h3>Conexión</h3>
	<div id="connections">
		<li>
		<strong>Http:</strong>&#160;<xsl:value-of select="c:http"/>
		<strong>webService:</strong>&#160;<xsl:value-of select="c:webService"/>
		<strong>numClients:</strong>&#160;<xsl:value-of select="c:numClients"/>
		</li>	
	</div>
	</xsl:template>
	
	<xsl:template match="c:database">
	<h3>Database</h3>
	<div id="database">
		<li>
		<strong>User:</strong>&#160;<xsl:value-of select="c:user"/>
		<strong>Password:</strong>&#160;<xsl:value-of select="c:password"/>
		<strong>URL:</strong>&#160;<xsl:value-of select="c:url"/>
		</li>	
	</div>
	</xsl:template>
	
	<xsl:template match="c:servers">
	<h3>Servers</h3>
	<div id="servers">
		<li>
		<xsl:apply-templates select="c:servers/c:server"/>
		</li>	
	</div>
	</xsl:template>
	
	<xsl:template match="c:server">
	<h4>Server</h4>
	<div id="server">
		<li>
		<strong>Name:</strong>&#160;<xsl:value-of select="@name"/>
		<strong>Wsdl:</strong>&#160;<xsl:value-of select="@wsdl"/>
		<strong>Namespace:</strong>&#160;<xsl:value-of select="@namespace"/>
		<strong>Service:</strong>&#160;<xsl:value-of select="@service"/>
		<strong>httpAddress:</strong>&#160;<xsl:value-of select="@httpAddress"/>
		</li>	
	</div>
	</xsl:template>
	
</xsl:stylesheet>