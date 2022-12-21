local task_key = KEYS[1]
local payload = ARGV[1]

local current_value = redis.call("GET", task_key)

if not current_value or current_value == nil or current_value == '' then
    redis.call("SET", task_key, payload)
    return 1
end
local json = cjson.decode(current_value)
if json.status == 'COMPLETED' then
    return 0
end

redis.call("SET", task_key, payload)

return 1