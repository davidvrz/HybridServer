<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:conf="http://www.esei.uvigo.es/dai/hybridserver"
                exclude-result-prefixes="conf">
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <html>
            <head>
                <title>Configuration Details</title>
            </head>
            <body>
                <h1>Configuration</h1>

                <h2>Connections</h2>
                <ul>
                    <li>HTTP Port: <xsl:value-of select="conf:configuration/conf:connections/conf:http"/></li>
                    <li>Webservice URL: <xsl:value-of select="conf:configuration/conf:connections/conf:webservice"/></li>
                    <li>Number of Clients: <xsl:value-of select="conf:configuration/conf:connections/conf:numClients"/></li>
                </ul>

                <h2>Database</h2>
                <ul>
                    <li>User: <xsl:value-of select="conf:configuration/conf:database/conf:user"/></li>
                    <li>Password: <xsl:value-of select="conf:configuration/conf:database/conf:password"/></li>
                    <li>URL: <xsl:value-of select="conf:configuration/conf:database/conf:url"/></li>
                </ul>

                <h2>Servers</h2>
                <table border="1">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>WSDL</th>
                            <th>Namespace</th>
                            <th>Service</th>
                            <th>HTTP Address</th>
                        </tr>
                    </thead>
                    <tbody>
                        <xsl:for-each select="conf:configuration/conf:servers/conf:server">
                            <tr>
                                <td><xsl:value-of select="@name"/></td>
                                <td><xsl:value-of select="@wsdl"/></td>
                                <td><xsl:value-of select="@namespace"/></td>
                                <td><xsl:value-of select="@service"/></td>
                                <td><xsl:value-of select="@httpAddress"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
