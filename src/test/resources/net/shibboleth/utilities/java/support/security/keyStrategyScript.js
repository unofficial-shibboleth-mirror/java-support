var Pair = Java.type("net.shibboleth.utilities.java.support.collection.Pair");

if (typeof name != "undefined" && name != null) {
    custom.get(name);
} else {
    new Pair(custom.get("default"), custom.get(custom.get("default")));
}
