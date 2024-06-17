import React from 'react';
import CircularProgress from '@mui/material/CircularProgress';
import Box from '@mui/material/Box';

function WithLoading(Component) {
  return function WithLoadingComponent({ isLoading, ...props }) {
    if (!isLoading) return <Component {...props} />;
    return (
      <Box sx={{ class:"App-Progress" }}>
        <CircularProgress />
      </Box>
    );
  };
}
export default WithLoading;
