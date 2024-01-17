(ns tiny.ui.components.platforms)

(defn firebase-provider [ctx] nil)
(defn local-store-provider [ctx] nil)
(defn session-store-provider [ctx] nil)
(defn keyboard-shortcuts-provider [ctx] nil)
(defn reset-provider [ctx] nil)

(def manifest {:platform/authentication-provider firebase-provider
               :platform/local-store-provider local-store-provider
               :platform/session-store-provider session-store-provider
               :platform/session-store-provider keyboard-shortcuts-provider
               :platform/reset-provider reset-provider})
