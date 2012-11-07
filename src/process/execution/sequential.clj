(ns process.execution.sequential
  (:use [process.definition :only [build-process-graph fnc?]]
        [process.execution :only [check-for-missing-dependencies
                                  invoke-fnc]]
        [process.graph.traversal :only [level-order
                                        unique-sets]]))

(defn execute-sequential
  "Executes the given process up to the point where the given output is calculated. The
   execution of the process is done sequentially. The execution assumes that every
   output in the existing-outputs map has already been calculated. The function returns
   a new map with the existing-outputs plus the calculated output and all outputs of
   the dependencies that had to be calculated to execute the calculation of the given
   output."
  [process-definition existing-outputs output]
  (check-for-missing-dependencies (merge process-definition existing-outputs))
  (let [graph (build-process-graph process-definition)
        execution-path (->> (level-order graph output)
                            unique-sets
                            (apply concat)
                            reverse)]
    (reduce
     (fn [existing-outputs output]
       (if-not (get existing-outputs output)
         (let [component (get process-definition output)]
           (assoc existing-outputs output
                  (if (fnc? component)
                    (invoke-fnc component existing-outputs)
                    component)))
         existing-outputs))
     existing-outputs execution-path)))