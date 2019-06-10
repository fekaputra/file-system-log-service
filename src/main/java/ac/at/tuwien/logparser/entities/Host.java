package ac.at.tuwien.logparser.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Agnes on 02.09.18.
 */
public class Host {

    private String ipAddress;
    private String ip4Address;
    private String ip6Address;
    private String hostname;
    @JsonProperty(value = "@type")
    private String type;

    public Host(){}

    public Host(String ipAddress, String ip4Address, String ip6Address, String hostname) {
        this.ipAddress = ipAddress;
        this.ip4Address = ip4Address;
        this.ip6Address = ip6Address;
        this.hostname = hostname;
    }

    public Host(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIp4Address() {
        return ip4Address;
    }

    public void setIp4Address(String ip4Address) {
        this.ip4Address = ip4Address;
    }

    public String getIp6Address() {
        return ip6Address;
    }

    public void setIp6Address(String ip6Address) {
        this.ip6Address = ip6Address;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Host{" +
                "ipAddress='" + ipAddress + '\'' +
                ", ip4Address='" + ip4Address + '\'' +
                ", ip6Address='" + ip6Address + '\'' +
                ", hostname='" + hostname + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

}
