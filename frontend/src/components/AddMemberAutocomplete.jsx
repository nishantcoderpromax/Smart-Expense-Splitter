import { useState } from "react";
import { Autocomplete, TextField, Box, Typography } from "@mui/material";
import { searchUsers } from "../api/userApi";

export default function AddMemberAutocomplete({ onSelect }) {
  const [options, setOptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [resetKey, setResetKey] = useState(0);
  let debounceRef;

  const handleInputChange = (_e, value) => {
    clearTimeout(debounceRef);
    if (!value.trim()) { setOptions([]); return; }
    debounceRef = setTimeout(() => {
      setLoading(true);
      searchUsers(value).then((r) => { setOptions(r); setLoading(false); });
    }, 250);
  };

  const handlePick = (user) => {
    onSelect(user);
    setOptions([]);
    setResetKey((k) => k + 1); // remounts the field so it clears after each add
  };

  return (
    <Autocomplete
      key={resetKey}
      options={options}
      loading={loading}
      filterOptions={(x) => x} // server already filtered; don't re-filter client-side
      getOptionLabel={(u) => u.name || ""}
      onInputChange={handleInputChange}
      onChange={(_e, user) => { if (user) handlePick(user); }}
      isOptionEqualToValue={(a, b) => a.id === b.id}
      renderOption={(props, u) => (
        <Box component="li" {...props} key={u.id}>
          <Box>
            <Typography sx={{ fontSize: "0.92rem" }}>{u.name}</Typography>
            <Typography sx={{ fontSize: "0.8rem", color: "var(--color-ink-soft)" }}>{u.email}</Typography>
          </Box>
        </Box>
      )}
      sx={{ flex: 1, minWidth: 260 }}
      renderInput={(params) => <TextField {...params} label="Search by name or email" />}
    />
  );
}
