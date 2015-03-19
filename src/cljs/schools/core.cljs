(ns schools.core
  (:require [schools.data]
            [cljs.reader]))

(cljs.reader/read-string "[:a]")

(js/console.log "Got schools " (count schools.data/schools))



